package com.oneline.shimpyo.domain.wish;

import com.oneline.shimpyo.domain.base.BaseEntity;
import com.oneline.shimpyo.domain.house.House;
import com.oneline.shimpyo.domain.member.Member;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter @Setter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "WISH")
@AllArgsConstructor
@Builder
@ToString
public class Wish extends BaseEntity {

    @MapsId("member_id")
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @MapsId("house_id")
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "house_id")
    private House house;

    @EmbeddedId
    private WishList wishList;
}
