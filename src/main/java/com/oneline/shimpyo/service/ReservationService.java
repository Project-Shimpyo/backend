package com.oneline.shimpyo.service;

import com.oneline.shimpyo.domain.BaseException;
import com.oneline.shimpyo.domain.GetPageRes;
import com.oneline.shimpyo.domain.reservation.ReservationStatus;
import com.oneline.shimpyo.domain.reservation.dto.*;
import com.siot.IamportRestClient.exception.IamportResponseException;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface ReservationService {

    GetPrepareReservationRes prepareReservation(long memberId) throws BaseException;
    long createReservation(long memberId, PostReservationReq postReservationReq)
            throws BaseException, IamportResponseException, IOException;
    GetPageRes<GetReservationListRes> readReservationList(long memberId, ReservationStatus reservationStatus, Pageable pageable);
    GetReservationRes readReservation(long memberId, long reservationId);
    GetHouseReservationRes readHouseReservationList(long memberId, long houseId,
                                                    ReservationStatus reservationStatus, Pageable pageable);
    void updateReservationPeopleCount(long memberId, long reservationId, int peopleCount);
    void cancelReservation(long memberId, long reservationId, PatchReservationReq patchReservationReq)
            throws BaseException, IamportResponseException, IOException;

    void updateReservationStatus(long memberId, long reservationId);
}
