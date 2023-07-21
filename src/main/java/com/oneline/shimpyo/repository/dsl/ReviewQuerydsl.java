package com.oneline.shimpyo.repository.dsl;

import com.oneline.shimpyo.domain.house.QHouse;
import com.oneline.shimpyo.domain.member.QMember;
import com.oneline.shimpyo.domain.member.QMemberImage;
import com.oneline.shimpyo.domain.review.Review;
import com.oneline.shimpyo.domain.review.ReviewRating;
import com.oneline.shimpyo.domain.review.dto.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.oneline.shimpyo.domain.house.QHouse.house;
import static com.oneline.shimpyo.domain.member.QMember.member;
import static com.oneline.shimpyo.domain.member.QMemberImage.memberImage;
import static com.oneline.shimpyo.domain.reservation.QReservation.reservation;
import static com.oneline.shimpyo.domain.review.QReview.review;

@Repository
public class ReviewQuerydsl {
    private final JPAQueryFactory jqf;

    public ReviewQuerydsl(EntityManager em) {
        this.jqf = new JPAQueryFactory(em);
    }

    public List<GetReviewRes> readReviewList(long memberId) {
        return jqf.select(new QGetReviewRes(reservation.id, review.id, reservation.createdDate,
                        review.lastModifiedDate, review.reviewRating, review.contents))
                .from(review)
                .join(review.reservation, reservation)
                .where(review.member.id.eq(memberId))
                .fetch();
    }

    public Slice<GetHouseReviewListRes.Review> findAllByHouseId(long houseId, Pageable pageable) {
        List<GetHouseReviewListRes.Review> reviews = jqf.select(new QGetHouseReviewListRes_Review(
                review.id, member.nickname, memberImage.savedFileName,
                        review.createdDate, review.lastModifiedDate, review.reviewRating, review.contents))
                .from(review)
                .join(review.member, member)
                .leftJoin(member.memberImage, memberImage)
                .where(review.house.id.eq(houseId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if(reviews.size() > pageable.getPageSize()){
            reviews.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(reviews, pageable, hasNext);
    }

    public List<ReviewStatusCount> findCountByHouseId(long houseId){
        return jqf.select(new QReviewStatusCount(review.reviewRating, review.count()))
                .from(review)
                .where(review.house.id.eq(houseId))
                .groupBy(review.reviewRating)
                .fetch();
    }


    public long likeReviewCount(long houseId){
        return jqf.select(review.count())
                .from(review)
                .where(review.house.id.eq(houseId))
                .where(review.reviewRating.eq(ReviewRating.GOOD))
                .fetchFirst();
    }
}
