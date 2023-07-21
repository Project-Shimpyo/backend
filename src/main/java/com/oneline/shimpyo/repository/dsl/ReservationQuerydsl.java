package com.oneline.shimpyo.repository.dsl;

import com.oneline.shimpyo.domain.reservation.ReservationStatus;
import com.oneline.shimpyo.domain.reservation.dto.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.oneline.shimpyo.domain.house.QHouse.house;
import static com.oneline.shimpyo.domain.house.QHouseAddress.houseAddress;
import static com.oneline.shimpyo.domain.house.QHouseImage.houseImage;
import static com.oneline.shimpyo.domain.member.QMember.member;
import static com.oneline.shimpyo.domain.pay.QPayMent.payMent;
import static com.oneline.shimpyo.domain.reservation.QReservation.reservation;
import static com.oneline.shimpyo.domain.review.QReview.review;
import static com.oneline.shimpyo.domain.room.QRoom.room;
import static com.oneline.shimpyo.domain.room.QRoomImage.roomImage;

@Repository
public class ReservationQuerydsl {
    private final JPAQueryFactory jqf;

    public ReservationQuerydsl(EntityManager em) {
        this.jqf = new JPAQueryFactory(em);
    }

    public Page<GetReservationListRes> readReservationList(long memberId, ReservationStatus reservationStatus, Pageable pageable) {
        List<GetReservationListRes> reservationList = jqf.select(new QGetReservationListRes(reservation.id,
                        houseImage.savedURL, house.name, member.nickname, house.type,
                        reservation.checkInDate, reservation.checkOutDate, room.checkIn, room.checkOut,
                        houseAddress.lat, houseAddress.lng, houseAddress.fullAddress,
                        reservation.reservationStatus, review.id))
                .from(reservation)
                .join(reservation.room, room)
                .join(room.house, house)
                .join(house.member, member)
                .join(house.images, houseImage)
                .join(house.houseAddress, houseAddress)
                .leftJoin(reservation.review, review)
                .groupBy(reservation)
                .where(reservation.member.id.eq(memberId))
                .where(reservationEq(reservationStatus))
                .orderBy(reservation.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jqf.select(reservation.count())
                .from(reservation)
                .where(reservation.member.id.eq(memberId));

        return PageableExecutionUtils.getPage(reservationList, pageable, countQuery::fetchOne);
    }

    public GetReservationRes readReservation(long reservationId) {
        return jqf.select(new QGetReservationRes(house.id, reservation.id, reservation.reservationStatus,
                        house.member.id, house.name,
                        house.member.nickname, reservation.checkInDate, reservation.checkOutDate,
                        room.checkIn, room.checkOut, room.id, room.name, room.maxPeople, room.minPeople,
                        reservation.peopleCount, houseAddress.lat, houseAddress.lng, houseAddress.fullAddress,
                        payMent.price, payMent.remainPrice, review.id))
                .from(reservation)
                .join(reservation.room, room)
                .join(room.house, house)
                .join(house.houseAddress, houseAddress)
                .join(reservation.payMent, payMent)
                .leftJoin(reservation.review, review)
                .where(reservation.id.eq(reservationId))
                .fetchFirst();

    }

    public Page<HostReservationReq> readHouseReservationList(long houseId,
                                                             ReservationStatus reservationStatus, Pageable pageable) {

        List<HostReservationReq> reservationList = jqf.select(new QHostReservationReq(reservation.id, reservation.reservationStatus, roomImage.savedURL, room.name,
                        reservation.checkInDate, reservation.checkOutDate, room.checkIn, room.checkOut,
                        member.nickname, reservation.peopleCount, reservation.phoneNumber))
                .from(reservation)
                .join(reservation.member, member)
                .join(reservation.room, room)
                .join(room.images, roomImage)
                .join(room.house, house).on(house.id.eq(houseId))
                .where(reservation.reservationStatus.eq(reservationStatus))
                .groupBy(reservation)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jqf.select(reservation.count())
                .from(reservation)
                .join(reservation.room, room)
                .join(room.house, house).on(house.id.eq(houseId))
                .where(reservation.reservationStatus.eq(reservationStatus));

        return PageableExecutionUtils.getPage(reservationList, pageable, countQuery::fetchOne);

    }

    public List<ReservationStatusCount> readHouseReservationStatusCount(long finalHouseId) {
        return jqf.select(new QReservationStatusCount(reservation.reservationStatus, reservation.count()))
                .from(reservation)
                .join(reservation.room, room)
                .join(room.house, house)
                .where(house.id.eq(finalHouseId))
                .groupBy(reservation.reservationStatus)
                .fetch();
    }

    private BooleanExpression reservationEq(ReservationStatus reservationStatus){
        if(reservationStatus.equals(ReservationStatus.COMPLETE)){
            return reservation.reservationStatus.eq(reservationStatus)
                    .or(reservation.reservationStatus.eq(ReservationStatus.USING));
        }
        return reservation.reservationStatus.eq(reservationStatus);
    }
}
