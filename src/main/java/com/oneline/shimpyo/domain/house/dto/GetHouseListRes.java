package com.oneline.shimpyo.domain.house.dto;

import com.oneline.shimpyo.domain.house.HouseType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetHouseListRes {
    private long houseId;
    private String name;
    private HouseType type;
    private int price;
    private String sido;
    private String sigungu;
    private long roomId;
    private double ratio;
    private boolean soldout;
    private boolean wished;
    private List<String> houseImages;

    @QueryProjection
    public GetHouseListRes(long houseId, String name, HouseType type, int price, String sido, String sigungu, long roomId, double ratio) {
        this.houseId = houseId;
        this.name = name;
        this.type = type;
        this.price = price;
        this.sido = sido;
        this.sigungu = sigungu;
        this.roomId = roomId;
        this.ratio = ratio;
        this.soldout = false;
        this.wished = false;
    }
}
