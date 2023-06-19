package com.oneline.shimpyo.service.impl;

import com.oneline.shimpyo.domain.BaseException;
import com.oneline.shimpyo.domain.coupon.Coupon;
import com.oneline.shimpyo.domain.pay.PayMent;
import com.oneline.shimpyo.domain.pay.PayStatus;
import com.oneline.shimpyo.domain.reservation.Reservation;
import com.oneline.shimpyo.domain.reservation.ReservationStatus;
import com.oneline.shimpyo.domain.reservation.dto.PostReservationReq;
import com.oneline.shimpyo.domain.reservation.dto.PatchReservationReq;
import com.oneline.shimpyo.domain.room.Room;
import com.oneline.shimpyo.repository.*;
import com.oneline.shimpyo.service.PaymentService;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static com.oneline.shimpyo.domain.BaseResponseStatus.*;

@Log4j2
@Service
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private static final long EMPTY_ID = 0L;
    private static final int FULL_REFUND = 0;
    private static final String API_KEY = "6354023824364652";
    private static final String API_SECRET = "oCGC7lJGsgCzkJG6i9JyIKwU1MtNE5SBJ7GnkCVqVzxbqaLo3DxIY5CwUQAoq5kqxsCXo2iQS4v2oeu6";
    private final IamportClient iamportClient;
    private final HouseRepository houseRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    public PaymentServiceImpl(RoomRepository roomRepository, MemberRepository memberRepository,
                              CouponRepository couponRepository, PaymentRepository paymentRepository,
                              HouseRepository houseRepository, ReservationRepository reservationRepository) {
        this.iamportClient = new IamportClient(API_KEY, API_SECRET);
        this.houseRepository = houseRepository;
        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
        this.couponRepository = couponRepository;
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public PayMent createPayment(long memberId, PostReservationReq postReservationReq)
            throws IamportResponseException, IOException, BaseException {
        checkDuplicatePayment(postReservationReq);
        int paidPrice = validatePaid(memberId, postReservationReq);

        PayMent payment = PayMent.builder().impUid(postReservationReq.getImpUid()).UUID(postReservationReq.getMerchantUid())
                .payMethod(postReservationReq.getPayMethod()).price(paidPrice).payStatus(PayStatus.COMPLETE).build();
        paymentRepository.save(payment);

        return payment;
    }

    @Override
    public Reservation cancelPayment(long memberId, long reservationId, PatchReservationReq patchReservationReq)
            throws IamportResponseException, IOException, BaseException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BaseException(RESERVATION_NONEXISTENT));
        PayMent payMent = reservation.getPayMent();

        validateCancel(memberId, patchReservationReq, reservation, payMent);

        IamportResponse<Payment> response = iamportClient.paymentByImpUid(payMent.getImpUid());
        CancelData cancelData = createCancelData(response, patchReservationReq.getRefundAmount());
        iamportClient.cancelPaymentByImpUid(cancelData);

        payMent.setPayStatus(PayStatus.CANCEL);

        return reservation;
    }

    private static void validateCancel(long memberId, PatchReservationReq patchReservationReq,
                                       Reservation reservation, PayMent payMent) {
        if(memberId != reservation.getMember().getId()){
            throw new BaseException(INVALID_MEMBER);
        }

        if (reservation.getReservationStatus() == ReservationStatus.CANCEL) {
            throw new BaseException(RESERVATION_CANCEL);
        }

        if (payMent.getPrice() < patchReservationReq.getRefundAmount()) {
            throw new BaseException(REFUND_WRONG);
        }
    }

    private CancelData createCancelData(IamportResponse<Payment> response, int refundAmount) {
        CancelData cancelData;

        if (refundAmount == FULL_REFUND) {
            cancelData = new CancelData(response.getResponse().getImpUid(), true);
        } else {
            cancelData = new CancelData(response.getResponse().getImpUid(), true, new BigDecimal(refundAmount));
        }

        return cancelData;
    }

    private void checkDuplicatePayment(PostReservationReq postReservationReq) throws BaseException {
        Optional<PayMent> payment = paymentRepository
                .duplicateCheck(postReservationReq.getMerchantUid(), postReservationReq.getImpUid());

        if (payment.isPresent()) {
            log.error("결제 중복 : impUid = " + postReservationReq.getImpUid() + "," +
                    " UUID = " + postReservationReq.getMerchantUid());
            throw new BaseException(PAYMENT_DUPLICATE);
        }
    }

    private int validatePaid(long memberId, PostReservationReq postReservationReq)
            throws BaseException, IOException, IamportResponseException {
        int priceToPay;
        Room room = roomRepository.findById(postReservationReq.getRoomId())
                .orElseThrow(() -> new BaseException(ROOM_NONEXISTENT));
        priceToPay = room.getPrice();

        int paidPrice = getPaidPriceInPortone(postReservationReq.getImpUid());

        //회원 등급 체크 todo clear later for test
        /*Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));
        MemberGrade memberGrade = member.getMemberGrade();
        priceToPay -= priceToPay / memberGrade.getDiscount();*/

        //쿠폰 체크
        if (postReservationReq.getCouponId() != EMPTY_ID) {
            Coupon coupon = couponRepository.findById(postReservationReq.getCouponId())
                    .orElseThrow(() -> new BaseException(COUPON_NONEXISTENT));
            priceToPay -= priceToPay * coupon.getDiscount();
        }

        //실제 결제 금액, db 금액 비교
        if (paidPrice != priceToPay) {
            log.error("결제 금액이 다릅니다. impUid = " + postReservationReq.getImpUid() +
                    ", paidPrice = " + paidPrice + ", priceToPay = " + priceToPay);
            // 금액이 다르면 결제 취소
            IamportResponse<Payment> response = iamportClient.paymentByImpUid(postReservationReq.getImpUid());
            CancelData cancelData = createCancelData(response, FULL_REFUND);
            iamportClient.cancelPaymentByImpUid(cancelData);

            throw new BaseException(PAYMENT_WRONG);
        }
        return paidPrice;
    }

    private int getPaidPriceInPortone(String impUid) throws IOException, IamportResponseException {
        Payment response = iamportClient.paymentByImpUid(impUid).getResponse();
        return (response.getAmount()).intValue();
    }

}
