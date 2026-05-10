package com.gym.mall.service.Impl;

import com.gym.mall.converter.CommodityConverter;
import com.gym.mall.dao.Commodity;
import com.gym.mall.Repository.CommodityRepository;
import com.gym.mall.dto.commodityDTO;
import com.gym.mall.service.CommodityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.gym.mall.Constants.COMMODITY_KEY;

@Service
@Slf4j
//标记一个类作为服务层
public class CommodityServiceImpl implements CommodityService {

    @Autowired
    private CommodityRepository commodityRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    //向编译器声明当前的方法意图覆盖（重写）父类或接口中的方法
    public Long addCommodity(commodityDTO commodityDTO) {
        List<Commodity> commodityList = commodityRepository.findByName(commodityDTO.getName());
        if(!CollectionUtils.isEmpty(commodityList)){
            throw new IllegalStateException("id"+commodityDTO.getId()+"has been taken");
        }
        Commodity commodity = commodityRepository.save(CommodityConverter.converterCommodity(commodityDTO));
        //从前端接收commodityDTO(数据传输对象),需要将DTO转换为(领域模型)Commodity才能保存到数据库
        //输入操作：DTO → Entity
        return commodity.getId();
        //返回新创建资源的 ID 是 RESTful 风格和业务开发中的标准做法
    }

    @Override
    public commodityDTO getCommodityById(Long commodityId) {
        String key = COMMODITY_KEY + commodityId;
        
        // 1. 先从 Redis 获取
        commodityDTO cachedCommodity = (commodityDTO) redisTemplate.opsForValue().get(key);
        if (cachedCommodity != null) {
            log.info("get commodity from cache, id: {}", commodityId);
            return cachedCommodity;
        }

        // 2. 缓存没有，查数据库
        log.info("get commodity from db, id: {}", commodityId);
        Commodity commodity = commodityRepository.findById(commodityId).orElseThrow(RuntimeException::new);
        commodityDTO dto = CommodityConverter.converterCommodity(commodity);

        // 3. 存入 Redis，设置过期时间（如 10 分钟），防止缓存一直占用空间
        redisTemplate.opsForValue().set(key, dto, 10, TimeUnit.MINUTES);
        
        return dto;
    }

    @Override
    public commodityDTO updateCommodityById(Long id, String name, String price) {
        Commodity commodityInDB = commodityRepository.findById(id).orElseThrow(RuntimeException::new);
        if (StringUtils.hasLength(name) && !commodityInDB.getName().equals(name)) {
            commodityInDB.setName(name);
        }
        if (StringUtils.hasLength(price) && !commodityInDB.getPrice().equals(price)) {
            commodityInDB.setPrice(price);
        }
        Commodity commodity = commodityRepository.save(commodityInDB);
        
        // 更新后删除缓存，保证下次查询获取最新数据
        redisTemplate.delete(COMMODITY_KEY + id);
        log.info("update commodity and delete cache, id: {}", id);
        
        return CommodityConverter.converterCommodity(commodity);
    }
    //get和update从数据库获取的是 Commodity实体
    //需要将Entity转换为DTO才能返回给前端
    //这是一个输出操作：Entity → DTO

    @Override
    public void deleteCommodityById(Long id) {
        commodityRepository.findById(id).orElseThrow(() -> new IllegalStateException("id:" + id + "is not exist"));
        commodityRepository.deleteById(id);
        
        // 删除后清理缓存
        redisTemplate.delete(COMMODITY_KEY + id);
        log.info("delete commodity and delete cache, id: {}", id);
    }
}
