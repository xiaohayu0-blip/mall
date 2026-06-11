package com.gym.mall.service.Impl;

import com.gym.mall.Repository.LikesStatisticRepository;
import com.gym.mall.Repository.LikesUserRecordRepository;
import com.gym.mall.domain.entity.LikesStatistic;
import com.gym.mall.domain.entity.LikesUserRecord;
import com.gym.mall.domain.dto.LikesUserRecordDTO;
import com.gym.mall.service.LikesService;
import com.gym.mall.service.RabbitmqService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;

import static com.gym.mall.Constants.LIKE_STATISTIC_KEY;
import static com.gym.mall.Constants.LIKE_USER_KEY;

@Service
@Slf4j
public class LikesServiceImpl implements LikesService {

    @Autowired
    private RabbitmqService  rabbitmqService;
    // RabbitMQ 消息服务，用于异步发送点赞消息

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    // Redis 模板，用于缓存点赞数据，提高查询性能

    @Autowired
    private LikesUserRecordRepository likesUserRecordRepository;

    @Autowired
    private LikesStatisticRepository likesStatisticRepository;

    @Override
    public boolean addNewLikesRecord(LikesUserRecordDTO likesUserRecordDTO) {
        rabbitmqService.publishMessage(likesUserRecordDTO);
        return true;
    }

    @Override
    public List<Long> getMyLikes(long userId, Long businessId) {
        // 创建一个空列表，用于存放用户点赞过的项目ID
        List<Long> likedItemIdList= Lists.newArrayList();
        // 从Redis缓存中获取该用户的所有点赞记录（使用Set数据结构存储）
        Set<String> allMyLikes = redisTemplate.opsForSet().members(LIKE_USER_KEY+userId);
        // 判断Redis缓存中是否有数据（不为null且不为空）
        if(allMyLikes!=null&&!allMyLikes.isEmpty()){
            // 遍历缓存中的每一条点赞记录
            for(String like:allMyLikes){
                // 将每条记录按":"分割，取第二部分（itemId），转成Long类型后添加到列表中
                likedItemIdList.add(Long.valueOf(like.split(":")[1]));
            }
            // 记录日志：从缓存加载数据
            log.info("load my likes from cache:businessId:{},userId:{}",businessId,userId);
            // 直接返回从缓存获取的点赞列表
            return likedItemIdList;
        }
        // 如果缓存没有数据，从数据库查询该用户在指定业务下的所有点赞记录
        List<LikesUserRecord> likesUserRecordDTOList=likesUserRecordRepository
                .findByUserIdAndBusinessIdAndLikes(userId,businessId,true);

        // 记录日志：从数据库加载数据
        log.info("load my likes from db:businessId:{},userId:{}",businessId,userId);
        // 判断数据库查询结果是否为null，如果是则返回空列表，否则将实体列表转换为itemId列表返回
        return likesUserRecordDTOList==null?Lists.newArrayList():
                likesUserRecordDTOList.stream().map(LikesUserRecord::getItemId).toList();
    }

    @Override
    public Long getItemLikesCount(Long businessId, Long itemId) {
        // 构建Redis缓存的key，格式为：LIKE_STATISTIC_KEY + businessId + ":" + itemId
        String statisticKey = LIKE_STATISTIC_KEY+businessId+":"+itemId;
        // 从Redis缓存中获取点赞数
        Long count=(Long) redisTemplate.opsForValue().get(statisticKey);
        // 判断缓存是否命中
        if(count!=null){
            // 缓存命中，记录日志并直接返回缓存数据
            log.info("load item like count from cache:businessId:{},itemId:{}",businessId,itemId);
            return count;
        }
        // 缓存未命中，从数据库查询点赞统计记录
        LikesStatistic likesStatistic=likesStatisticRepository.findByBusinessIdAndItemId(businessId,itemId).orElse(null);
        // 记录日志：从数据库加载数据
        log.info("load item like count from db:businessId:{},itemId:{}",businessId,itemId);
        // 返回结果：如果数据库中没有记录则返回0，否则返回实际点赞数
        return likesStatistic==null?0L:likesStatistic.getLikeCount();
    }

    @Override
    public boolean hasLiked(long userId, long businessId, long itemId) {
        String key = LIKE_USER_KEY + userId;
        String value = businessId + ":" + itemId;

        // Redis Set 的 SISMEMBER 操作：判断元素是否在集合中
        // 时间复杂度 O(1)，极其高效
        Boolean isMember = redisTemplate.opsForSet().isMember(key, value);
        
        if (Boolean.TRUE.equals(isMember)) {
            log.info("check like status from redis: user {} has liked item {}", userId, itemId);
            return true;
        }

        // 缓存没有，查数据库
        log.info("check like status from db: user {} for item {}", userId, itemId);
        return likesUserRecordRepository.findUserLikeRecord(userId, businessId, itemId).isPresent();
    }
}
