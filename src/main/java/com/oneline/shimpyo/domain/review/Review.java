package com.oneline.shimpyo.domain.review;

import com.oneline.shimpyo.domain.base.BaseEntity;
import com.oneline.shimpyo.domain.house.House;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.reservation.Reservation;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@AllArgsConstructor
@Getter @Setter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "REVIEW")
public class Review extends BaseEntity {
    @Id @GeneratedValue
    @Column(name = "review_id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "house_id")
    private House house;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @NotNull
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @NotNull
    private String contents;

    @NotNull
    @Enumerated(value = STRING)
    private ReviewRating reviewRating;

    @Builder
    public Review(House house, Member member, Reservation reservation, String contents, ReviewRating reviewRating) {
        setHouseAndMember(house, member);
        this.reservation = reservation;
        this.contents = contents;
        this.reviewRating = reviewRating;

    }

    public void setHouseAndMember(House house, Member member){
        this.house = house;
        this.member = member;
        house.getReviews().add(this);
        member.getReviews().add(this);
    }
}
