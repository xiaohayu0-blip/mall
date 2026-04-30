package com.gym.mall.Repository;

import com.gym.mall.dao.LikesStatistic;
import com.gym.mall.dto.LikesUserRecordDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LIkesStatisticRepository extends JpaRepository<LikesStatistic,Long> , JpaSpecificationExecutor<LikesStatistic> {

    Optional<LikesStatistic> findByBusinessIdAndItemId(long businessId,long itemId);
}
