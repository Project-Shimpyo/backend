package com.oneline.shimpyo.service.impl;

import com.oneline.shimpyo.domain.BaseException;
import com.oneline.shimpyo.domain.house.HouseImage;
import com.oneline.shimpyo.domain.pay.PayMent;
import com.oneline.shimpyo.domain.reservation.NonMemberReservation;
import com.oneline.shimpyo.domain.reservation.Reservation;
import com.oneline.shimpyo.domain.reservation.ReservationStatus;
import com.oneline.shimpyo.domain.reservation.dto.GetReservationRes;
import com.oneline.shimpyo.domain.reservation.dto.PatchReservationReq;
import com.oneline.shimpyo.domain.reservation.dto.PostReservationReq;
import com.oneline.shimpyo.domain.room.Room;
import com.oneline.shimpyo.repository.*;
import com.oneline.shimpyo.repository.dsl.NonMemberReservationQuerydsl;
import com.oneline.shimpyo.service.NonMemberReservationService;
import com.oneline.shimpyo.service.PaymentService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.oneline.shimpyo.domain.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NonMemberReservationServiceImpl implements NonMemberReservationService {

    private final PaymentService paymentService;
    private final NonMemberReservationRepository nmrRepository;
    private final NonMemberReservationQuerydsl nmrQuerydsl;
    private final HouseImageRepository houseImageRepository;
    private final RoomRepository roomRepository;

    @Transactional
    @Override
    public String createReservation(PostReservationReq postReservationReq) throws IamportResponseException, IOException {
        PayMent payment = paymentService.createNonMemberPayment(postReservationReq);

        Room room = roomRepository.findById(postReservationReq.getRoomId())
                .orElseThrow(() -> new BaseException(ROOM_NONEXISTENT));

        if(room.getMaxPeople() < postReservationReq.getPeopleCount()){
            throw new BaseException(RESERVATION_WRONG_PEOPLE_COUNT);
        }

        NonMemberReservation reservation = NonMemberReservation.builder().room(room).payMent(payment)
                .reservationStatus(ReservationStatus.COMPLETE)
                .name(postReservationReq.getName())
                .phoneNumber(postReservationReq.getPhoneNumber())
                .peopleCount(postReservationReq.getPeopleCount())
                .checkInDate(postReservationReq.stringToLocalDateTime(postReservationReq.getCheckInDate()))
                .checkOutDate(postReservationReq.stringToLocalDateTime(postReservationReq.getCheckOutDate()))
                .build();
        nmrRepository.save(reservation);

        return payment.getUUID();
    }

    @Override
    public GetReservationRes readReservation(String merchantUid) {
        NonMemberReservation reservation = nmrRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new BaseException(RESERVATION_NONEXISTENT));

        GetReservationRes getReservationRes = nmrQuerydsl.readReservation(reservation.getId());
        List<HouseImage> houseImageList = houseImageRepository.findByHouseId(getReservationRes.getHouseId());
        getReservationRes.setHouseImageUrl(houseImageList.stream().map(HouseImage::getSavedURL).collect(Collectors.toList()));

        return getReservationRes;
    }

    @Transactional
    @Override
    public void updateReservationPeopleCount(String merchantUid, int peopleCount) {
        NonMemberReservation reservation = nmrRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new BaseException(RESERVATION_NONEXISTENT));
        Room room = reservation.getRoom();

        if (reservation.getReservationStatus() != ReservationStatus.COMPLETE) {
            throw new BaseException(RESERVATION_CANCEL_OR_FINISHED);
        }
        if(room.getMaxPeople() < peopleCount){
            throw new BaseException(RESERVATION_WRONG_PEOPLE_COUNT);
        }

        reservation.setPeopleCount(peopleCount);
    }

    @Transactional
    @Override
    public void cancelReservation(String merchantUid, PatchReservationReq patchReservationReq)
            throws IamportResponseException, IOException {
        NonMemberReservation findReservation = nmrRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new BaseException(RESERVATION_NONEXISTENT));
        NonMemberReservation reservation = paymentService.cancelNonMemberPayment(findReservation, patchReservationReq);

        reservation.setReservationStatus(ReservationStatus.CANCEL);
    }
}
