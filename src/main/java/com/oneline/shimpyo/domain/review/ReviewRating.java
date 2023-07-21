package com.oneline.shimpyo.domain.review;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.oneline.shimpyo.domain.BaseException;
import lombok.Getter;

import java.util.stream.Stream;

import static com.oneline.shimpyo.domain.BaseResponseStatus.REVIEW_RATING_WRONG;

@Getter
public enum ReviewRating {
    GOOD("좋아요"), BAD("별로에요");

    private String status;

    ReviewRating(String status) {
        this.status = status;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ReviewRating findByMethod(String method) throws BaseException {
        return Stream.of(ReviewRating.values())
                .filter(c -> c.name().equals(method))
                .findFirst()
                .orElseThrow(() -> new BaseException(REVIEW_RATING_WRONG));
    }
}
