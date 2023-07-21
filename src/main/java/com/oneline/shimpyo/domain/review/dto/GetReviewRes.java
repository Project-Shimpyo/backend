package com.oneline.shimpyo.domain.review.dto;

import com.oneline.shimpyo.domain.review.ReviewRating;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetReviewRes {

    private long reservationId;
    private long reviewId;
    private String reviewCreatedDate;
    private String reviewModifiedDate;
    private ReviewRating reviewRating;
    private String contents;

    @QueryProjection
    public GetReviewRes(long reservationId, long reviewId, LocalDateTime reviewCreatedDate,
                        LocalDateTime reviewModifiedDate, ReviewRating reviewRating, String contents) {
        this.reservationId = reservationId;
        this.reviewId = reviewId;
        this.reviewCreatedDate = reviewCreatedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        this.reviewModifiedDate = reviewModifiedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        this.reviewRating = reviewRating;
        this.contents = contents;
    }

}
