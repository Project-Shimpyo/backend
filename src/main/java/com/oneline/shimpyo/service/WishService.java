package com.oneline.shimpyo.service;

import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.wish.dto.GetWishRes;
import com.oneline.shimpyo.domain.wish.dto.PostWishReq;

import java.util.List;

public interface WishService {
    void createWish(Member member, PostWishReq postWishReq);

    List<GetWishRes> readWishList(Member member);

    void deleteWish(Member member, long houseId);

}
