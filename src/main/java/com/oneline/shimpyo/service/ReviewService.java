package com.oneline.shimpyo.service;

import com.oneline.shimpyo.domain.review.dto.GetHouseReviewListRes;
import com.oneline.shimpyo.domain.review.dto.GetReviewRes;
import com.oneline.shimpyo.domain.review.dto.PatchReviewReq;
import com.oneline.shimpyo.domain.review.dto.PostReviewReq;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    void createReview(long memberId, PostReviewReq postReviewReq);

    void updateReview(long reviewId, PatchReviewReq patchReviewReq);

    void deleteReview(long reviewId);

    List<GetReviewRes> readReviewList(long memberId);

    GetHouseReviewListRes readHouseReviewList(long houseId, Pageable pageable);
}
