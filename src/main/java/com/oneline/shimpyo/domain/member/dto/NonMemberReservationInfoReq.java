package com.oneline.shimpyo.domain.member.dto;

import lombok.Data;

@Data
public class NonMemberReservationInfoReq {
    String phoneNumber;
    String reservationCode;
}
