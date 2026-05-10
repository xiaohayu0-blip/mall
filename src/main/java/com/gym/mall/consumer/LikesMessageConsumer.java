package com.gym.mall.consumer;

import com.google.gson.Gson;
import com.gym.mall.Repository.LIkesStatisticRepository;
import com.gym.mall.Repository.LikesUserRecordRepository;
import com.gym.mall.converter.LikesUserRecordConverter;
import com.gym.mall.dao.LikesStatistic;
import com.gym.mall.dao.LikesUserRecord;
import com.gym.mall.dto.LikesUserRecordDTO;
import com.gym.mall.validator.LikesValidator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.gym.mall.Constants.LIKE_STATISTIC_KEY;
import static com.gym.mall.Constants.LIKE_USER_KEY;

//消息消费者(consumer)的常见用途：
//
//- 异步处理耗时操作（如点赞统计、数据库更新等）
//- 解耦系统，提高响应速度
//- 实现削峰填谷，保护数据库
@Component
//将该类标记为 Spring 容器管理的 Bean，可以被自动注入到其他组件中
@Slf4j
//自动生成日志对象 log，方便记录日志
@Data
//自动生成 getter、setter、toString、equals、hashCode 等方法
public class LikesMessageConsumer {

    @Value("${rabbitmq.commodity.queue}")
    private String queueName;

    @Value("${rabbitmq.commodity.exchange}")
    private String exchange;

    @Autowired
    private LikesUserRecordRepository likesUserRecordRepository;

    @Autowired
    private LIkesStatisticRepository likesStatisticRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LikesValidator likesValidator;

    private Gson gson=new Gson();
    //创建 Google Gson 对象实例，用于 JSON 序列化和反序列化（将消息转换为 Java 对象，或反之）

    /**
     * 处理点赞消息的消费者方法
     * 从 RabbitMQ 队列中接收点赞记录消息，根据用户是否已点赞来执行点赞或取消点赞操作
     * 
     * @param likesUserRecordDTO 包含点赞信息的数据传输对象，包含用户ID、业务ID、商品ID等信息
     */
    @RabbitListener(queues="${rabbitmq.commodity.queue}")
    //指定要监听的队列名称，从配置文件中读取
    public void handleMessage(LikesUserRecordDTO likesUserRecordDTO) {
        // 记录接收到的消息日志，方便调试和追踪
        log.info("Received message from queue{}:message:{}", queueName, gson.toJson(likesUserRecordDTO));
        
        // 对点赞数据进行参数校验，确保数据完整性和正确性
        likesValidator.validateAddNewCommodity(likesUserRecordDTO);
        
        // 数据库查询用户是否已经点过赞
        likesUserRecordRepository.findUserLikeRecord(likesUserRecordDTO.getUserId(), likesUserRecordDTO.getBusinessId(),
                likesUserRecordDTO.getItemId())
                // 使用 ifPresentOrElse 判断：
                // - 如果找到了点赞记录（用户已点赞），则执行取消点赞操作
                // - 如果没找到（用户未点赞），则执行点赞操作
                .ifPresentOrElse(
                    likesUserRecord -> doUnLikeAction(likesUserRecord),
                    () -> doLikeAction(likesUserRecordDTO)
                );
    }

    private void doLikeAction(LikesUserRecordDTO likesUserRecordDTO) {
        // 将 DTO 转换为数据库实体对象
        LikesUserRecord likesUserRecord = LikesUserRecordConverter.convertToLikesUserRecord(likesUserRecordDTO);
        // 保存点赞记录到数据库
        likesUserRecordRepository.save(likesUserRecord);

        // 构造 Redis 中点赞统计数据的 Key
        String likesStatisticKey=LIKE_STATISTIC_KEY+likesUserRecordDTO.getBusinessId()+":"+likesUserRecordDTO.getItemId();

        // 根据业务 ID 和商品 ID 查询点赞统计记录
        likesStatisticRepository.findByBusinessIdAndItemId(likesUserRecordDTO.getBusinessId(),
                        likesUserRecordDTO.getItemId())
                .ifPresentOrElse(likesStatistic-> {
                    // 记录已存在，点赞数 +1
                    long newLikeCount=likesStatistic.getLikeCount()+1;
                    likesStatistic.setLikeCount(newLikeCount);
                    // 更新数据库
                    likesStatisticRepository.save(likesStatistic);
                    // 同步更新 Redis
                    redisTemplate.opsForValue().set(likesStatisticKey,newLikeCount);
                }, () -> {
                    // 记录不存在，创建新的点赞统计记录
                    LikesStatistic likesStatistic=LikesStatistic.builder()
                            .businessId(likesUserRecordDTO.getBusinessId())
                            .itemId(likesUserRecordDTO.getItemId())
                            .likeCount(1L).build();

                    // 保存到数据库
                    likesStatisticRepository.save(likesStatistic);
                    // 同步到 Redis，初始值为 1
                    redisTemplate.opsForValue().set(likesStatisticKey,1L);
                });

        // 将用户点赞记录添加到 Redis Set 中，防止重复点赞
        redisTemplate.opsForSet().add(LIKE_USER_KEY + likesUserRecordDTO.getUserId(), likesUserRecordDTO.getBusinessId() + ":" +
                likesUserRecordDTO.getItemId());
    }
    private void doUnLikeAction(LikesUserRecord likesUserRecord) {
        likesUserRecord.setLikes(false);
        likesUserRecordRepository.save(likesUserRecord);

        String likesStatisticKey = LIKE_STATISTIC_KEY + likesUserRecord.getBusinessId() + ":" + likesUserRecord.getItemId();
        likesStatisticRepository.findByBusinessIdAndItemId(likesUserRecord.getBusinessId(),
                        likesUserRecord.getItemId())
                .ifPresentOrElse(likesStatistic -> {
                    long newLikeCount = likesStatistic.getLikeCount() - 1;
                    likesStatistic.setLikeCount(newLikeCount);
                    likesStatisticRepository.save(likesStatistic);

                    redisTemplate.opsForValue().set(likesStatisticKey, newLikeCount);
                }, () -> {
                    log.info("there is no likes statistic for businessId:{}, itemId:{}",
                            likesUserRecord.getBusinessId(), likesUserRecord.getItemId());
                });

        redisTemplate.opsForSet().remove(LIKE_USER_KEY + likesUserRecord.getUserId(), likesUserRecord.getBusinessId() + ":" +
                likesUserRecord.getItemId());

    }
}
