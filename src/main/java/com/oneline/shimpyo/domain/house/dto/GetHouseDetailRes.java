package com.oneline.shimpyo.domain.house.dto;

import com.oneline.shimpyo.domain.room.dto.RoomInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Builder
public class GetHouseDetailRes {
    private HouseInfo house;
    private List<RoomInfo> rooms;


}
