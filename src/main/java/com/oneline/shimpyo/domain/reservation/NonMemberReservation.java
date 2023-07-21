package com.oneline.shimpyo.domain.reservation;

import com.oneline.shimpyo.domain.base.BaseEntity;
import com.oneline.shimpyo.domain.pay.PayMent;
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
@Table(name = "NON_MEMBER_RESERVATION")
public class NonMemberReservation extends BaseEntity {
    @Id @GeneratedValue
    @Column(name = "non_member_id")
    private Long id;

    @NotNull
    @OneToOne(cascade = ALL)
    @JoinColumn(name = "pay_id")
    private PayMent payMent;

    @NotNull
    private String name;

    private String reservationCode;

    @NotNull
    private String phoneNumber;

    @NotNull
    private int peopleCount;

    @NotNull
    private LocalDateTime checkInDate;

    @NotNull
    private LocalDateTime checkOutDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
}
