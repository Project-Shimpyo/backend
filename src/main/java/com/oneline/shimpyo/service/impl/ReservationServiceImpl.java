package com.oneline.shimpyo.service.impl;

import com.oneline.shimpyo.domain.BaseException;
import com.oneline.shimpyo.domain.GetPageRes;
import com.oneline.shimpyo.domain.coupon.dto.GetCouponRes;
import com.oneline.shimpyo.domain.house.HouseImage;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.member.MemberGrade;
import com.oneline.shimpyo.domain.pay.PayMent;
import com.oneline.shimpyo.domain.pay.PayStatus;
import com.oneline.shimpyo.domain.reservation.Reservation;
import com.oneline.shimpyo.domain.reservation.ReservationStatus;
import com.oneline.shimpyo.domain.reservation.dto.*;
import com.oneline.shimpyo.domain.room.Room;
import com.oneline.shimpyo.modules.aop.Retry;
import com.oneline.shimpyo.modules.aop.RetryAspect;
import com.oneline.shimpyo.repository.*;
import com.oneline.shimpyo.repository.dsl.MyCouponQuerydsl;
import com.oneline.shimpyo.repository.dsl.ReservationQuerydsl;
import com.oneline.shimpyo.service.PaymentService;
import com.oneline.shimpyo.service.ReservationService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.oneline.shimpyo.domain.BaseResponseStatus.*;

@Import(RetryAspect.class)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private final PaymentService paymentService;
    private final ReservationRepository reservationRepository;
    private final ReservationQuerydsl reservationQuerydsl;
    private final HouseRepository houseRepository;
    private final MemberRepository memberRepository;
    private final HouseImageRepository houseImageRepository;
    private final RoomRepository roomRepository;
    private final MyCouponQuerydsl myCouponQuerydsl;

    @Override
    public GetPrepareReservationRes prepareReservation(long memberId) throws BaseException {
        String uuid = UUID.randomUUID().toString();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));
        MemberGrade memberGrade = member.getMemberGrade();
        List<GetCouponRes> myCouponList = myCouponQuerydsl.getMyCouponList(memberId);

        return new GetPrepareReservationRes(uuid, memberGrade.getGrade().getRank(), memberGrade.getDiscount(), myCouponList);
    }

    @Retry
    @Transactional
    @Override
    public long createReservation(long memberId, PostReservationReq postReservationReq)
            throws BaseException, IamportResponseException, IOException {
        PayMent payment = paymentService.createMemberPayment(memberId, postReservationReq);

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));
        Room room = roomRepository.findByIdWithLock(postReservationReq.getRoomId())
                .orElseThrow(() -> new BaseException(ROOM_NONEXISTENT));

        checkCanReservation(memberId, postReservationReq, room);

        Reservation reservation = Reservation.builder().room(room).member(member).payMent(payment)
                .peopleCount(postReservationReq.getPeopleCount()).phoneNumber(member.getPhoneNumber())
                .reservationStatus(ReservationStatus.COMPLETE)
                .checkInDate(postReservationReq.stringToLocalDateTime(postReservationReq.getCheckInDate()))
                .checkOutDate(postReservationReq.stringToLocalDateTime(postReservationReq.getCheckOutDate()))
                .build();
        reservationRepository.save(reservation);
        room.setTotalCount(room.getTotalCount() - 1);

        return reservation.getId();
    }

    @Override
    public GetPageRes<GetReservationListRes> readReservationList(long memberId, ReservationStatus reservationStatus,
                                                                 Pageable pageable) {
        memberRepository.findById(memberId).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));

        return new GetPageRes<>(reservationQuerydsl.readReservationList(memberId, reservationStatus, pageable));
    }

    @Override
    public GetReservationRes readReservation(long memberId, long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BaseException(RESERVATION_NONEXISTENT));

        validateMember(memberId, reservation.getMember().getId());

        GetReservationRes getReservationRes = reservationQuerydsl.readReservation(reservationId);
        List<HouseImage> houseImageList = houseImageRepository.findByHouseId(getReservationRes.getHouseId());
        getReservationRes.setHouseImageUrl(houseImageList.stream().map(HouseImage::getSavedURL).collect(Collectors.toList()));

        return getReservationRes;
    }

    @Override
    public GetHouseReservationRes readHouseReservationList(long memberId, long houseId,
                                                           ReservationStatus reservationStatus, Pageable pageable) {
        houseRepository.findById(houseId).orElseThrow(() -> new BaseException(HOUSE_NONEXISTENT));

        Page<HostReservationReq> hostReservationReqs = reservationQuerydsl
                .readHouseReservationList(houseId, reservationStatus, pageable);

        List<ReservationStatusCount> statusCountList = reservationQuerydsl.readHouseReservationStatusCount(houseId);

        return new GetHouseReservationRes(hostReservationReqs, statusCountList);
    }

    @Transactional
    @Override
    public void updateReservationPeopleCount(long memberId, long reservationId,
                                             int peopleCount) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BaseException(RESERVATION_NONEXISTENT));
        Room room = reservation.getRoom();

        validateMember(memberId, reservation.getMember().getId());

        if (reservation.getReservationStatus() != ReservationStatus.COMPLETE) {
            throw new BaseException(RESERVATION_CANCEL_OR_FINISHED);
        }

        if (room.getMaxPeople() < peopleCount) {
            throw new BaseException(RESERVATION_WRONG_PEOPLE_COUNT);
        }

        reservation.setPeopleCount(peopleCount);
    }

    @Transactional
    @Override
    public void cancelReservation(long memberId, long reservationId, PatchReservationReq patchReservationReq)
            throws BaseException, IamportResponseException, IOException {
        Reservation reservation = paymentService.cancelMemberPayment(memberId, reservationId, patchReservationReq);

        reservation.setReservationStatus(ReservationStatus.CANCEL);
    }

    @Transactional
    @Override
    public void updateReservationStatus(long memberId, long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BaseException(RESERVATION_NONEXISTENT));

        reservation.getRoom().setTotalCount(reservation.getRoom().getTotalCount() + 1);
        reservation.setReservationStatus(ReservationStatus.FINISHED);
    }

    private void checkCanReservation(long memberId, PostReservationReq postReservationReq, Room room) {
        if (room.getHouse().getMember().getId() == memberId) {
            throw new BaseException(RESERVATION_CANT_MY_HOUSE);
        }

        if (room.getMaxPeople() < postReservationReq.getPeopleCount()) {
            throw new BaseException(RESERVATION_WRONG_PEOPLE_COUNT);
        }

        if (room.getTotalCount() <= 0) {
            throw new BaseException(RESERVATION_ROOM_COUNT);
        }
    }

    private void validateMember(long requestMemberId, long dbMemberId) {
        if (requestMemberId != dbMemberId) {
            throw new BaseException(INVALID_MEMBER);
        }
    }
}
