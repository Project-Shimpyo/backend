package com.oneline.shimpyo.repository.dsl;

import com.oneline.shimpyo.domain.member.Member;

import com.oneline.shimpyo.domain.member.dto.*;

import com.oneline.shimpyo.domain.reservation.Reservation;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;

import static com.oneline.shimpyo.domain.member.QMember.*;
import static com.oneline.shimpyo.domain.member.QMemberImage.*;
import static com.oneline.shimpyo.domain.reservation.QNonMemberReservation.*;
import static com.oneline.shimpyo.domain.reservation.QReservation.*;
import static com.oneline.shimpyo.domain.reservation.ReservationStatus.FINISHED;
import static com.oneline.shimpyo.domain.reservation.ReservationStatus.USING;

@Repository
public class MemberQuerydsl {
    private final JPAQueryFactory jqf;

    public MemberQuerydsl(EntityManager em) {
        this.jqf = new JPAQueryFactory(em);
    }


    public List<Member> findByEmailListWithPhoneNumber(String phoneNumber) {
        return jqf.select(member)
                .from(member)
                .where(member.phoneNumber.eq(phoneNumber))
                .fetch();
    }

    public Member findByMemberWithPhoneNumber(String phoneNumber) {
        return jqf.select(member)
                .from(member)
                .where(member.social.eq(false)
                        .and(member.phoneNumber.eq(phoneNumber)))
                .fetchOne();
    }

    public MemberInfoRes findMemberInfo(Member info) {
        return jqf.select(new QMemberInfoRes(member.nickname, member.email, member.phoneNumber, member.id))
                .from(member)
                .where(member.id.eq(info.getId())).fetchOne();


    }

    public List<Reservation> findByMemberReservationInfo(Member info) {
        return jqf.select(reservation)
                .from(reservation)
                .join(reservation.member, member).fetchJoin()
                .where(reservation.reservationStatus.eq(USING).and(reservation.reservationStatus.eq(FINISHED)))
                .fetch();
    }

    public MemberProfileRes findMemberProfile(Long memberId) {
        return jqf.select(new QMemberProfileRes(memberImage.savedPath, member.comments, member.id))
                .from(member)
                .join(memberImage)
                .on(member.id.eq(memberImage.member.id))
                .where(member.id.eq(memberId))
                .fetchOne();
    }
}
