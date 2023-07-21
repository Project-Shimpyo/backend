package com.oneline.shimpyo.domain.house.dto;

import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RoomReq {
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
    private int imageCount;
    private List<FileReq> files;
}
