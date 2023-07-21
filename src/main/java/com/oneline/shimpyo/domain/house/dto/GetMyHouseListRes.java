package com.oneline.shimpyo.domain.house.dto;

import com.oneline.shimpyo.domain.house.HouseType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@NoArgsConstructor
public class GetMyHouseListRes {

    private long id;
    private String name;
    private String imageUrl;
    private HouseType houseType;

    @QueryProjection
    public GetMyHouseListRes(long id, String name, String imageUrl, HouseType houseType) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.houseType = houseType;
    }
}
