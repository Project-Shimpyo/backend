package com.oneline.shimpyo.controller;

import com.oneline.shimpyo.domain.BaseException;
import com.oneline.shimpyo.domain.BaseResponse;
import com.oneline.shimpyo.domain.GetPageRes;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.reservation.ReservationStatus;
import com.oneline.shimpyo.domain.reservation.dto.*;
import com.oneline.shimpyo.modules.CheckMember;
import com.oneline.shimpyo.security.auth.CurrentMember;
import com.oneline.shimpyo.service.ReservationService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final CheckMember checkMember;

    @GetMapping("/prepare")
    public BaseResponse<GetPrepareReservationRes> prepareReservation(@CurrentMember Member member) throws BaseException {
        long memberId = checkMember.getMemberId(member, true);

        return new BaseResponse<>(reservationService.prepareReservation(memberId));
    }

    @PostMapping("")
    public BaseResponse<PostReservationRes> createReservation(@RequestBody PostReservationReq postReservationReq,
                                                              @CurrentMember Member member)
            throws IamportResponseException, BaseException, IOException {
        long memberId = checkMember.getMemberId(member, true);
        long reservationId = reservationService.createReservation(memberId, postReservationReq);
        return new BaseResponse<>(new PostReservationRes(reservationId));
    }

    @GetMapping("")
    public BaseResponse<GetPageRes<GetReservationListRes>> readReservationList(@CurrentMember Member member,
                                                                               @RequestParam(required = false,
                                                                                       defaultValue = "COMPLETE")
                                                                               String reservationStatus,
                                                                               @PageableDefault Pageable pageable) {
        long memberId = checkMember.getMemberId(member, true);
        checkMember.checkCurrentMember(member, memberId);
        return new BaseResponse<>(reservationService.readReservationList(memberId,
                ReservationStatus.of(reservationStatus),pageable));
    }

    @GetMapping("/{reservationId}")
    public BaseResponse<GetReservationRes> readReservation(@PathVariable long reservationId,
                                                           @CurrentMember Member member) {
        long memberId = checkMember.getMemberId(member, true);
        GetReservationRes getReservationRes = reservationService.readReservation(memberId, reservationId);

        return new BaseResponse<>(getReservationRes);
    }

    @GetMapping("/houses/{houseId}")
    public BaseResponse<GetHouseReservationRes> readHouseReservationList(@PathVariable long houseId,
                                                                         @RequestParam(required = false,
                                                                                 defaultValue = "USING")
                                                                         String reservationStatus,
                                                                         @PageableDefault Pageable pageable,
                                                                         @CurrentMember Member member){
        long memberId = checkMember.getMemberId(member, true);

        GetHouseReservationRes getHouseReservationRes = reservationService
                .readHouseReservationList(memberId, houseId,
                        ReservationStatus.of(reservationStatus), pageable);
        return new BaseResponse<>(getHouseReservationRes);
    }

    @PatchMapping("/{reservationId}")
    public BaseResponse<Void> updateReservationPeopleCount(@PathVariable long reservationId,
                                                           @RequestBody PatchReservationPeopleReq patchReservationPeopleReq,
                                                           @CurrentMember Member member){
        long memberId = checkMember.getMemberId(member, true);
        reservationService.updateReservationPeopleCount(memberId, reservationId, patchReservationPeopleReq.getPeopleCount());
        return new BaseResponse<>();
    }

    @DeleteMapping("/{reservationId}")
    public BaseResponse<Void> cancelReservation(@PathVariable long reservationId,
                                                @RequestBody PatchReservationReq patchReservationReq,
                                                @CurrentMember Member member)
            throws BaseException, IamportResponseException, IOException {
        long memberId = checkMember.getMemberId(member, true);
        reservationService.cancelReservation(memberId, reservationId, patchReservationReq);
        return new BaseResponse<>();
    }

    @PatchMapping("/{reservationId}/status")
    public BaseResponse<Void> updateReservationStatus(@PathVariable long reservationId, @CurrentMember Member member){
        long memberId = checkMember.getMemberId(member, true);
        reservationService.updateReservationStatus(memberId, reservationId);
        return new BaseResponse<>();
    }
}
