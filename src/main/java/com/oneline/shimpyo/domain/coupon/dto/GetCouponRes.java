package com.oneline.shimpyo.domain.coupon.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ToString
@Getter
@NoArgsConstructor
public class GetCouponRes {

    private long couponId;
    private String name;
    private String description;
    private int discount;
    private String expiredDate;

    @QueryProjection
    public GetCouponRes(long couponId, String name, String description, int discount, LocalDateTime expiredDate) {
        this.couponId = couponId;
        this.name = name;
        this.description = description;
        this.discount = discount;
        this.expiredDate = expiredDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
}
