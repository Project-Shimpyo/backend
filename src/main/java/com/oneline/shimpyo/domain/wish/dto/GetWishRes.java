package com.oneline.shimpyo.domain.wish.dto;

import com.oneline.shimpyo.domain.house.HouseType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@Getter
@NoArgsConstructor
@Builder
@ToString
public class GetWishRes {
    private long houseId;
    private String houseName;
    private HouseType houseType;
    private String houseImage;

    @QueryProjection
    public GetWishRes(long houseId, String houseName, HouseType houseType, String houseImage) {
        this.houseId = houseId;
        this.houseName = houseName;
        this.houseType = houseType;
        this.houseImage = houseImage;
    }
}
