package com.oneline.shimpyo.domain.room.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class RoomInfo {
    private long roomId;
    private String name;
    private int price;
    private int minPeople;
    private int maxPeople;
    private int bedCount;
    private int bedroomCount;
    private int bathroomCount;
    private int totalCount;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private boolean soldout;
    private List<String> roomImages;

    @QueryProjection
    public RoomInfo(long roomId, String name, int price, int minPeople, int maxPeople, int bedCount, int bedroomCount, int bathroomCount,
                    int totalCount, LocalTime checkIn, LocalTime checkOut) {
        this.roomId = roomId;
        this.name = name;
        this.price = price;
        this.minPeople = minPeople;
        this.maxPeople = maxPeople;
        this.bedCount = bedCount;
        this.bedroomCount = bedroomCount;
        this.bathroomCount = bathroomCount;
        this.totalCount = totalCount;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.soldout = false;
    }
}
