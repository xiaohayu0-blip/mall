package com.gym.mall.service.Impl;

import com.gym.mall.dto.LikesUserRecordDTO;
import com.gym.mall.service.LikesService;
import com.gym.mall.service.RabbitmqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LikesServiceImpl implements LikesService {

    @Autowired
    private RabbitmqService  rabbitmqService;

    @Override
    public boolean addNewLikesRecord(LikesUserRecordDTO likesUserRecordDTO) {
        return false;
    }

    @Override
    public List<Long> getMyLikes(long userId, Long businessId) {
        return List.of();
    }

    @Override
    public Long getItemLikesCount(Long businessId, Long itemId) {
        return 0L;
    }
}
