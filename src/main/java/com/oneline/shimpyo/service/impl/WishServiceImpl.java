package com.oneline.shimpyo.service.impl;

import com.oneline.shimpyo.domain.BaseException;
import com.oneline.shimpyo.domain.house.House;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.wish.Wish;
import com.oneline.shimpyo.domain.wish.WishList;
import com.oneline.shimpyo.domain.wish.dto.GetWishRes;
import com.oneline.shimpyo.domain.wish.dto.PostWishReq;
import com.oneline.shimpyo.repository.HouseRepository;
import com.oneline.shimpyo.repository.WishRepository;
import com.oneline.shimpyo.repository.dsl.WishQuerydsl;
import com.oneline.shimpyo.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.oneline.shimpyo.domain.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishServiceImpl implements WishService {

    private final WishRepository wishRepository;
    private final WishQuerydsl wishQuerydsl;
    private final HouseRepository houseRepository;

    @Override
    @Transactional
    public void createWish(Member member, PostWishReq postWishReq) {
        Optional<Wish> foundWish = wishRepository.findByMemberIdAndHouseId(member.getId(), postWishReq.getHouseId());
        if (foundWish.isEmpty()) {
//            long count = wishQuerydsl.countByMemberIdAndHouseId(member.getId());
            if (wishQuerydsl.countByMemberIdAndHouseId(member.getId()) > 19) {
                throw new BaseException(WISH_COUNT_MAX);
            }
            House foundHouse = houseRepository.findById(postWishReq.getHouseId()).orElseThrow(() -> new BaseException(HOUSE_NONEXISTENT));

            WishList toSaveWishList = WishList.builder()
                    .member_id(member.getId())
                    .house_id(postWishReq.getHouseId())
                    .build();

            Wish toSaveWish = Wish.builder()
                    .member(member)
                    .house(foundHouse)
                    .wishList(toSaveWishList)
                    .build();
            wishRepository.save(toSaveWish);

        } else {
            throw new BaseException(WISH_ALREADY_EXIST);
        }
    }

    @Override
    public List<GetWishRes> readWishList(Member member) {
        return wishQuerydsl.findAllByMemberId(member.getId());
    }

    @Override
    @Transactional
    public void deleteWish(Member member, long houseId) {
        Optional<Wish> foundWish = wishRepository.findByMemberIdAndHouseId(member.getId(), houseId);
        if (foundWish.isPresent()) {
            wishRepository.deleteByMemberIdAndHouseId(member.getId(), houseId);
        } else {
            throw new BaseException(WISH_WRONG);
        }
    }
}
