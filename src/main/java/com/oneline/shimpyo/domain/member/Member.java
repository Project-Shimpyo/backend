package com.oneline.shimpyo.domain.member;

import com.oneline.shimpyo.domain.base.BaseEntity;
import com.oneline.shimpyo.domain.coupon.MyCoupon;
import com.oneline.shimpyo.domain.house.House;
import com.oneline.shimpyo.domain.house.dto.FileReq;
import com.oneline.shimpyo.domain.member.dto.MemberReq;
import com.oneline.shimpyo.domain.reservation.Reservation;
import com.oneline.shimpyo.domain.review.Review;
import com.oneline.shimpyo.domain.wish.Wish;
import com.oneline.shimpyo.security.CustomBCryptPasswordEncoder;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static com.oneline.shimpyo.domain.member.MemberRole.*;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Builder
@AllArgsConstructor
@Entity
@Getter @Setter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "MEMBER")
public class Member extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotNull
    private String email;
    @NotNull
    private String password;

    @NotNull
    private int point;

    private String nickname;

    private String phoneNumber;

    private String provider;
    private String providerId;
    private Boolean social;
    private String comments;

    @Lob
    private String refreshToken;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_grade_id")
    private MemberGrade memberGrade;

    @Enumerated(value = STRING)
    private MemberRole role;

    @OneToOne(mappedBy = "member", cascade = ALL, fetch = LAZY)
    private MemberImage memberImage;

    @OneToMany(mappedBy = "member", cascade = ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = ALL)
    private List<MyCoupon> myCoupons = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = ALL)
    private List<Wish> wishList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = ALL)
    private List<House> houseList = new ArrayList<>();

    @Builder
    public Member(String email, String password, String phoneNumber, int point, String provider, MemberGrade memberGrade, MemberRole role, boolean social) {
        this.email = email;
        this.password = password;
        this.point = point;
        this.provider = provider;
        this.memberGrade = memberGrade;
        this.role = role;
        this.social = social;
    }

    public Member(MemberReq request, CustomBCryptPasswordEncoder bCryptPasswordEncoder, MemberGrade memberGrade) {
        this.email = request.getEmail();
        this.password = bCryptPasswordEncoder.encode(request.getPassword());
        this.phoneNumber = request.getPhoneNumber();
        this.nickname = request.getNickname();
        this.point = 0;
        this.memberGrade = memberGrade;
        this.role = CLIENT;
        this.social = false;
        this.comments = " ";
        memberGrade.getMembers().add(this);

    }

    public Member(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Builder
    public Member(String email, String provider, String nickname) {
        this.email = email;
        this.provider = provider;
        this.nickname = nickname;
    }

    public void oAuthJoin(String phoneNumber, String nickname) {
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
        this.social = true;
    }

    public Member update(String email) {
        this.email = email;
        return this;
    }

    public void resetPassword(String password, CustomBCryptPasswordEncoder bCryptPasswordEncoder) {
        this.password = bCryptPasswordEncoder.encode(password);
    }

    public void updateRefreshToken(String newToken) {
        this.refreshToken = newToken;
    }

}
