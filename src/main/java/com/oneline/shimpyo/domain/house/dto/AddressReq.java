package com.oneline.shimpyo.domain.house.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AddressReq {
    private String postCode;
    private String sido;
    private String sigungu;
    private String fullAddress;
    private double lat;
    private double lng;
}
