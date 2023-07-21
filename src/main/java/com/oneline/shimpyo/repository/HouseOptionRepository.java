package com.oneline.shimpyo.repository;

import com.oneline.shimpyo.domain.house.HouseOptions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseOptionRepository extends JpaRepository<HouseOptions, Long> {
    void deleteAllByHouseId(Long id);

}
