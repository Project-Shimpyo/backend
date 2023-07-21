package com.oneline.shimpyo.repository;

import com.oneline.shimpyo.domain.coupon.Coupon;
import com.oneline.shimpyo.domain.wish.Wish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishRepository extends JpaRepository<Wish, Long> {
    Optional<Wish> findByMemberIdAndHouseId(Long id, long houseId);

    void deleteByMemberIdAndHouseId(Long id, long houseId);
}
