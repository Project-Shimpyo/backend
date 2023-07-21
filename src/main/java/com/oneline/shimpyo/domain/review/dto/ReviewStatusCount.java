package com.oneline.shimpyo.domain.review.dto;

import com.oneline.shimpyo.domain.review.ReviewRating;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewStatusCount {

    private ReviewRating reviewRating;
    private long reviewCount;

    @QueryProjection
    public ReviewStatusCount(ReviewRating reviewRating, long reviewCount) {
        this.reviewRating = reviewRating;
        this.reviewCount = reviewCount;
    }
}
