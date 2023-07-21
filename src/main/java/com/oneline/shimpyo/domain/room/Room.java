package com.oneline.shimpyo.domain.room;

import com.oneline.shimpyo.domain.base.BaseEntity;
import com.oneline.shimpyo.domain.house.House;
import com.oneline.shimpyo.domain.reservation.NonMemberReservation;
import com.oneline.shimpyo.domain.reservation.Reservation;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Builder
@Entity
@Getter @Setter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Table(name = "ROOM")
public class Room extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private int price;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "house_id")
    private House house;
    @NotNull
    private int minPeople;

    @NotNull
    private int maxPeople;

    @NotNull
    private int bedCount;
    @NotNull
    private int bedroomCount;
    @NotNull
    private int bathroomCount;

    @NotNull
    //제공할 수 있는 총 객실 수
    private int totalCount;
    
    private LocalTime checkIn;
    private LocalTime checkOut;

    @ColumnDefault("0")
    @Version
    private Long version;

    @OneToMany(mappedBy = "room", cascade = ALL)
    private List<RoomImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = ALL)
    private List<NonMemberReservation> nonMemberReservations = new ArrayList<>();

    @Builder
    public Room(Long id, String name, int price, House house, int minPeople, int maxPeople, int bedCount, int bedroomCount, int bathroomCount, int totalCount, LocalTime checkIn, LocalTime checkOut
                , List<RoomImage> images, List<Reservation> reservations, List<NonMemberReservation> nonMemberReservations) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.house = house;
        this.minPeople = minPeople;
        this.maxPeople = maxPeople;
        this.bedCount = bedCount;
        this.bedroomCount = bedroomCount;
        this.bathroomCount = bathroomCount;
        this.totalCount = totalCount;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.images = images;
        this.reservations = reservations;
        this.nonMemberReservations = nonMemberReservations;
    }

}
