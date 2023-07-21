package com.oneline.shimpyo.domain.house;

import com.oneline.shimpyo.domain.base.BaseEntity;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.review.Review;
import com.oneline.shimpyo.domain.room.Room;
import com.oneline.shimpyo.domain.wish.Wish;
import lombok.*;

import javax.annotation.meta.When;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.*;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter @Setter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "HOUSE")
@ToString
public class House extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "house_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String name;

    @Enumerated(value = STRING)
    private HouseType type;
    private String contents;    // 사장님 한마디
    @OneToOne(mappedBy = "house", cascade = ALL)
    private HouseAddress houseAddress;
    @OneToMany(mappedBy = "house", cascade = ALL)
    private List<HouseOptions> houseOptions = new ArrayList<>();
    @OneToMany(mappedBy = "house",cascade = ALL)
    private List<Room> rooms = new ArrayList<>();
    @OneToMany(mappedBy = "house", cascade = ALL)
    private List<HouseImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "house", cascade = ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "house", cascade = ALL)
    private List<Wish> wishList = new ArrayList<>();
    private double avgRating;





    @Builder
    public House(Long id, Member member, String name, HouseType type, List<HouseOptions> houseOptions, String contents, HouseAddress houseAddress
                , List<Room> rooms, List<HouseImage> images, List<Review> reviews, List<Wish> wishList, double avgRating) {
        this.id = id;
        this.member = member;
        this.name = name;
        this.type = type;
        this.houseOptions = houseOptions;
        this.contents = contents;
        this.houseAddress = houseAddress;
        this.rooms = rooms;
        this.images = images;
        this.reviews = reviews;
        this.wishList = wishList;
        this.avgRating = avgRating;
    }

}
