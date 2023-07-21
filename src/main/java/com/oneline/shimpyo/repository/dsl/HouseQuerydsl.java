package com.oneline.shimpyo.repository.dsl;

import com.oneline.shimpyo.domain.house.HouseType;
import com.oneline.shimpyo.domain.house.dto.*;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.reservation.ReservationStatus;
import com.oneline.shimpyo.domain.room.Room;

import com.oneline.shimpyo.domain.room.dto.QRoomInfo;
import com.oneline.shimpyo.domain.room.dto.RoomInfo;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;


import java.time.LocalDateTime;
import java.util.List;

import static com.oneline.shimpyo.domain.house.QHouse.house;
import static com.oneline.shimpyo.domain.house.QHouseAddress.houseAddress;
import static com.oneline.shimpyo.domain.house.QHouseImage.houseImage;
import static com.oneline.shimpyo.domain.house.QHouseOptions.houseOptions;
import static com.oneline.shimpyo.domain.reservation.QReservation.reservation;
import static com.oneline.shimpyo.domain.room.QRoom.room;
import static com.oneline.shimpyo.domain.room.QRoomImage.roomImage;
import static com.oneline.shimpyo.domain.wish.QWish.wish;

@Repository
public class HouseQuerydsl {
    private final JPAQueryFactory jqf;

    public HouseQuerydsl(EntityManager em) {
        this.jqf = new JPAQueryFactory(em);
    }

    public List<GetMyHouseListRes> readMyHouseList(long memberId) {
        return jqf.select(new QGetMyHouseListRes(house.id, house.name, houseImage.savedURL, house.type))
                .from(house)
                .join(house.images, houseImage)
                .where(house.member.id.eq(memberId))
                .groupBy(house)
                .fetch();
    }

    public HouseInfo findHouseAndAddressByHouseId(long houseId) {
        return jqf.select(new QHouseInfo(house.id, house.name, house.type, house.contents, houseAddress.postCode
                                                        , houseAddress.sido, houseAddress.sigungu, houseAddress.fullAddress, houseAddress.lat, houseAddress.lng))
                .from(house)
                .join(house.houseAddress, houseAddress)
                .on(house.id.eq(houseAddress.house.id))
                .where(house.id.eq(houseId))
                .fetchOne();
    }

    public List<String> findHouseImagesByHouseId(long houseId) {
        return jqf.select(houseImage.savedURL)
                .from(houseImage)
                .where(houseImage.house.id.eq(houseId))
                .fetch();
    }

    public List<String> findHouseOptionsByHouseId(long houseId) {
        return jqf.select(houseOptions.name)
                .from(houseOptions)
                .where(houseOptions.house.id.eq(houseId))
                .fetch();
    }

    public List<RoomInfo> findRoomsByHouseId(long houseId) {
        return jqf.select(new QRoomInfo(room.id, room.name, room.price, room.minPeople, room.maxPeople, room.bedCount, room.bedroomCount
                                    , room.bathroomCount, room.totalCount, room.checkIn, room.checkOut))
                .from(room)
                .where(room.house.id.eq(houseId))
                .fetch();
    }

    public List<String> findRoomImagesByRoomId(long roomId) {
        return jqf.select(roomImage.savedURL)
                .from(roomImage)
                .where(roomImage.room.id.eq(roomId))
                .fetch();
    }

    public List<GetHouseListRes> findAllHouse(Pageable pageable, SearchFilterReq searchFilter, Member member) {
        List<GetHouseListRes> foundHouseList = jqf.select(new QGetHouseListRes(house.id, house.name, house.type, room.price.min(), houseAddress.sido, houseAddress.sigungu, room.id.min(), house.avgRating))
                .from(house)
                .join(house.houseAddress, houseAddress)
                .on(house.id.eq(houseAddress.house.id))
                .join(house.rooms, room)
                .on(house.id.eq(room.house.id))
                .where(minPeopleLoe(searchFilter.getPeople()), maxPeopleGoe(searchFilter.getPeople()), typeEq(searchFilter.getType())
                        , cityEq(searchFilter.getCity()), districtEq(searchFilter.getDistrict()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .groupBy(house.id, houseAddress.sido, houseAddress.sigungu)
                .orderBy(house.avgRating.desc())
                .fetch();


        //이미지 저장 및 관심숙소 등록여부 검증
        for (GetHouseListRes houseRes : foundHouseList) {
            // 이미지 저장
            houseRes.setHouseImages(findHouseImagesByHouseId(houseRes.getHouseId()));
            
            // 관심숙소 등록여부 검증
            if (member != null) {
                long result = jqf.select(wish.member.id.count())
                        .from(wish)
                        .where(wish.member.id.eq(member.getId()), wish.house.id.eq(houseRes.getHouseId()))
                        .fetchOne();
                if (result > 0) houseRes.setWished(true);
            }
        }

        // 체크인, 체크아웃 기간이 명시되어 있을 경우
        if (searchFilter.getCheckin() != null && searchFilter.getCheckout() != null) {
            for (GetHouseListRes houseRes : foundHouseList) {
                if (checkReservation(houseRes.getRoomId(), searchFilter.getCheckin(), searchFilter.getCheckout())) houseRes.setSoldout(true);
//                Room foundRoom = jqf.selectFrom(room)
//                        .where(room.id.eq(houseRes.getRoomId()))
//                        .fetchOne();
//                long reservedCount = jqf.select(reservation.count())
//                        .from(reservation)
//                        .where(reservation.reservationStatus.eq(ReservationStatus.COMPLETE), reservation.room.id.eq(houseRes.getRoomId()),
//                                reservation.checkInDate.loe(searchFilter.getCheckin()),
//                                reservation.checkOutDate.goe(searchFilter.getCheckout()))
//                        .fetchOne();
//                if (foundRoom.getTotalCount() <= reservedCount) { // 얘약인원이 꽉 찬 경우 해당 숙소 정보를 목록에서 제외
//                    houseRes.setSoldout(true);
//                }
            }
        }

        return foundHouseList;
    }

    // 동적 Query DSL Where 조건
    private BooleanExpression minPeopleLoe(int minPeople) {
        return minPeople != 0 ? room.minPeople.loe(minPeople) : null;
    }
    private BooleanExpression maxPeopleGoe(int maxPeople) {
        return maxPeople != 0 ? room.maxPeople.goe(maxPeople) : null;
    }
    private BooleanExpression typeEq(HouseType type) {
        return type != null ? house.type.eq(type) : null;
    }
    private BooleanExpression cityEq(String city) {
        return city != null ? houseAddress.sido.eq(city) : null;
    }
    private BooleanExpression districtEq(String district) {
        return district != null ? houseAddress.sigungu.eq(district) : null;
    }

    // 예약 현황 확인
    public boolean checkReservation(long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        Room foundRoom = jqf.selectFrom(room)
                .where(room.id.eq(roomId))
                .fetchOne();
        long reservedCount = jqf.select(reservation.count())
                .from(reservation)
                .where(reservation.reservationStatus.eq(ReservationStatus.COMPLETE), reservation.room.id.eq(foundRoom.getId()),
                        reservation.checkInDate.loe(checkIn),
                        reservation.checkOutDate.goe(checkOut))
                .fetchOne();
        return (foundRoom.getTotalCount() <= reservedCount) ? true : false;
    }
}
