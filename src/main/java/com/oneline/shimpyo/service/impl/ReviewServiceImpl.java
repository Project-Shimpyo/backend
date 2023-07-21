package com.oneline.shimpyo.service.impl;

import com.oneline.shimpyo.domain.BaseException;
import com.oneline.shimpyo.domain.BaseResponseStatus;
import com.oneline.shimpyo.domain.house.House;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.reservation.Reservation;
import com.oneline.shimpyo.domain.review.Review;
import com.oneline.shimpyo.domain.review.dto.GetHouseReviewListRes;
import com.oneline.shimpyo.domain.review.dto.GetReviewRes;
import com.oneline.shimpyo.domain.review.dto.PatchReviewReq;
import com.oneline.shimpyo.domain.review.dto.PostReviewReq;
import com.oneline.shimpyo.repository.HouseRepository;
import com.oneline.shimpyo.repository.MemberRepository;
import com.oneline.shimpyo.repository.ReservationRepository;
import com.oneline.shimpyo.repository.ReviewRepository;
import com.oneline.shimpyo.repository.dsl.ReviewQuerydsl;
import com.oneline.shimpyo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.oneline.shimpyo.domain.BaseResponseStatus.*;
import static com.oneline.shimpyo.domain.reservation.ReservationStatus.FINISHED;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewQuerydsl reviewQuerydsl;
    private final HouseRepository houseRepository;

    @Transactional
    @Override
    public void createReview(long memberId, PostReviewReq postReviewReq) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));
        Reservation reservation = reservationRepository.findById(postReviewReq.getReservationId())
                .orElseThrow(() -> new BaseException(RESERVATION_NONEXISTENT));
        validateCreateReview(memberId, reservation);
        House house = reservation.getRoom().getHouse();

        Review review = Review.builder().member(member).house(house).reservation(reservation)
                .contents(postReviewReq.getContents()).reviewRating(postReviewReq.getReviewRating()).build();
        reviewRepository.save(review);

        double totalReviewCount = house.getReviews().size();
        double likeReviewCount = reviewQuerydsl.likeReviewCount(house.getId());
        double avgRating = (likeReviewCount / totalReviewCount) * 100;
        house.setAvgRating(avgRating);
    }

    @Override
    public List<GetReviewRes> readReviewList(long memberId) {
        return reviewQuerydsl.readReviewList(memberId);
    }

    @Transactional
    @Override
    public void updateReview(long reviewId, PatchReviewReq patchReviewReq) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.REVIEW_NONEXISTENT));
        House house = review.getHouse();

        review.setContents(patchReviewReq.getContents());
        review.setReviewRating(patchReviewReq.getReviewRating());

        double totalReviewCount = house.getReviews().size();
        double likeReviewCount = reviewQuerydsl.likeReviewCount(house.getId());
        double avgRating = (likeReviewCount / totalReviewCount) * 100;
        house.setAvgRating(avgRating);
    }

    @Transactional
    @Override
    public void deleteReview(long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.REVIEW_NONEXISTENT));

        House house = review.getHouse();
        double totalReviewCount = house.getReviews().size();
        double likeReviewCount = reviewQuerydsl.likeReviewCount(house.getId());
        double avgRating = (likeReviewCount / totalReviewCount) * 100;
        house.setAvgRating(avgRating);
        
        reviewRepository.delete(review);
    }

    @Override
    public GetHouseReviewListRes readHouseReviewList(long houseId, Pageable pageable) {
        houseRepository.findById(houseId).orElseThrow(() -> new BaseException(HOUSE_NONEXISTENT));
        Slice<GetHouseReviewListRes.Review> reviews = reviewQuerydsl.findAllByHouseId(houseId, pageable);
        return new GetHouseReviewListRes(reviews.hasNext(), reviews.getContent());
    }

    /**
     * 이미 있는 리뷰인지 확인
     */
    private void validateCreateReview(long memberId, Reservation reservation) {
        Optional<Review> review = reviewRepository.findByReservationId(reservation.getId());
        /*if(review.isPresent()){
            throw new BaseException(REVIEW_ALREADY_EXIST);
        }*/
        if (reservation.getReservationStatus() != FINISHED) {
            throw new BaseException(REVIEW_RESERVATION_WRONG_STATUS);
        }
        if (reservation.getMember().getId() != memberId) {
            throw new BaseException(INVALID_MEMBER);
        }
    }

}
