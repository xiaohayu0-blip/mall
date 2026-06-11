package com.gym.mall.Repository;

import com.gym.mall.domain.entity.LikesStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikesStatisticRepository extends JpaRepository<LikesStatistic, Long> {

    Optional<LikesStatistic> findByBusinessIdAndItemId(Long businessId, Long itemId);
}
