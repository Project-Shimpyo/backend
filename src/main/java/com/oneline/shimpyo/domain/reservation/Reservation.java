package com.oneline.shimpyo.domain.reservation;

import com.oneline.shimpyo.domain.base.BaseEntity;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.pay.PayMent;
import com.oneline.shimpyo.domain.review.Review;
import com.oneline.shimpyo.domain.room.Room;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Table(name = "RESERVATION")
@ToString
public class Reservation extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "reservation_id")
    private Long id;

    @NotNull
    @OneToOne(cascade = ALL)
    @JoinColumn(name = "pay_id")
    private PayMent payMent;

    @NotNull
    private String phoneNumber;

    @NotNull
    private int peopleCount;

    @NotNull
    private LocalDateTime checkInDate;

    @NotNull
    private LocalDateTime checkOutDate;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @OneToOne(mappedBy = "reservation")
    private Review review;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

}
