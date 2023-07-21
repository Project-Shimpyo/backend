package com.oneline.shimpyo.controller;

import com.oneline.shimpyo.domain.BaseResponse;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.wish.dto.GetWishRes;
import com.oneline.shimpyo.domain.wish.dto.PostWishReq;
import com.oneline.shimpyo.security.auth.CurrentMember;
import com.oneline.shimpyo.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/wish")
public class WishController {
    private final WishService wishService;

    @PostMapping("")
    public BaseResponse<Void> createWish(@CurrentMember Member member, @RequestBody PostWishReq postWishReq){
        wishService.createWish(member, postWishReq);
        return new BaseResponse<>();
    }

    @GetMapping("")
    public BaseResponse<List<GetWishRes>> readWishList(@CurrentMember Member member) {
        List<GetWishRes> wishList = wishService.readWishList(member);
        return new BaseResponse<>(wishList);
    }

    @DeleteMapping("/{houseId}")
    public BaseResponse<Void> deleteWish(@CurrentMember Member member, @PathVariable long houseId) {
        wishService.deleteWish(member, houseId);
        return new BaseResponse<>();
    }
}
