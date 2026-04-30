package com.gym.mall.Repository;

import com.gym.mall.dao.LikesUserRecord;
import com.gym.mall.dto.LikesUserRecordDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikesUserRecordRepository extends JpaRepository<LikesUserRecordDTO, Long> , JpaSpecificationExecutor<LikesUserRecord> {
    @Query(value="select*from likes_user_record where user_id=:userId and business_id=:businessId and item_id=:itemId and likes>0",nativeQuery = true)
    Optional<LikesUserRecord> findUserLikeRecord(long userId,long businessId,long itemId);

    List<LikesUserRecord> findByUserIdAndBusinessIdAndLikes(long userId,Long businessId,boolean likes);
}
