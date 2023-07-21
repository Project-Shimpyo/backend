package com.oneline.shimpyo.controller;

import com.oneline.shimpyo.domain.BaseResponse;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.review.dto.GetHouseReviewListRes;
import com.oneline.shimpyo.domain.review.dto.GetReviewRes;
import com.oneline.shimpyo.domain.review.dto.PatchReviewReq;
import com.oneline.shimpyo.domain.review.dto.PostReviewReq;
import com.oneline.shimpyo.modules.CheckMember;
import com.oneline.shimpyo.security.auth.CurrentMember;
import com.oneline.shimpyo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CheckMember checkMember;

    @PostMapping("/user/reviews")
    public BaseResponse<Void> createReview(@RequestBody PostReviewReq postReviewReq,
                                           @CurrentMember Member member){
        long memberId = checkMember.getMemberId(member, true);
        reviewService.createReview(memberId, postReviewReq);
        return new BaseResponse<>();
    }

    @GetMapping("/user/reviews")
    public BaseResponse<List<GetReviewRes>> readReviewList(
            @CurrentMember Member member){
        long memberId = checkMember.getMemberId(member, true);
        checkMember.checkCurrentMember(member, memberId);

        return new BaseResponse<>(reviewService.readReviewList(memberId));
    }

    @PatchMapping("/user/reviews/{reviewId}")
    public BaseResponse<Void> updateReview(@CurrentMember Member member, @PathVariable long reviewId,
                                           @RequestBody PatchReviewReq patchReviewReq){
        long memberId = checkMember.getMemberId(member, true);
        checkMember.checkCurrentMember(member, memberId);

        reviewService.updateReview(reviewId, patchReviewReq);
        return new BaseResponse<>();
    }

    @DeleteMapping("/user/reviews/{reviewId}")
    public BaseResponse<Void> deleteReview(@CurrentMember Member member, @PathVariable long reviewId){
        long memberId = checkMember.getMemberId(member, true);
        checkMember.checkCurrentMember(member, memberId);

        reviewService.deleteReview(reviewId);
        return new BaseResponse<>();
    }

    @GetMapping("/api/reviews/houses/{houseId}")
    public BaseResponse<GetHouseReviewListRes> readHouseReviewList(@PathVariable long houseId,
                                                                   @PageableDefault Pageable pageable){
        return new BaseResponse<>(reviewService.readHouseReviewList(houseId, pageable));
    }

}
