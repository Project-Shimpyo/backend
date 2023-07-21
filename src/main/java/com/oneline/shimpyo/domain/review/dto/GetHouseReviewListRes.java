package com.oneline.shimpyo.domain.review.dto;

import com.oneline.shimpyo.domain.review.ReviewRating;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetHouseReviewListRes {

    private boolean hasNext;
    private List<Review> review;

    @Getter
    @NoArgsConstructor
    public static class Review {
        private long reviewId;
        private String name;
        private String imageUrl;
        private String reviewCreatedDate;
        private String reviewModifiedDate;
        private ReviewRating reviewRating;
        private String contents;

        @QueryProjection
        public Review(long reviewId, String name, String imageUrl, LocalDateTime reviewCreatedDate,
                      LocalDateTime reviewModifiedDate, ReviewRating reviewRating, String contents) {
            this.reviewId = reviewId;
            this.name = name;
            this.imageUrl = imageUrl;
            this.reviewCreatedDate = reviewCreatedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            this.reviewModifiedDate = reviewModifiedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            this.reviewRating = reviewRating;
            this.contents = contents;
        }

    }
}
