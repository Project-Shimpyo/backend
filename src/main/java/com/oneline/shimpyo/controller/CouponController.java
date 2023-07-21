package com.oneline.shimpyo.controller;

import com.oneline.shimpyo.domain.BaseResponse;
import com.oneline.shimpyo.domain.coupon.dto.GetCouponRes;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.modules.CheckMember;
import com.oneline.shimpyo.security.auth.CurrentMember;
import com.oneline.shimpyo.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/coupons")
public class CouponController {

    private final CouponService couponService;
    private final CheckMember checkMember;

    @GetMapping("")
    public BaseResponse<List<GetCouponRes>> readCouponList(@CurrentMember Member member){
        long memberId = checkMember.getMemberId(member, true);
        return new BaseResponse<>(couponService.readCouponList(memberId));
    }

}
