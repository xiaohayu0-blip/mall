package com.gym.mall.service.Impl;

import com.gym.mall.converter.CommodityConverter;
import com.gym.mall.Repository.CategoryRepository;
import com.gym.mall.dao.Commodity;
import com.gym.mall.Repository.CommodityRepository;
import com.gym.mall.dto.CommodityPageRequest;
import com.gym.mall.dto.CommodityPageResponse;
import com.gym.mall.dto.commodityDTO;
import com.gym.mall.service.CommodityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.gym.mall.Constants.COMMODITY_KEY;

@Service
@Slf4j
public class CommodityServiceImpl implements CommodityService {

    @Autowired
    private CommodityRepository commodityRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Long addCommodity(commodityDTO commodityDTO) {
        List<Commodity> commodityList = commodityRepository.findByName(commodityDTO.getName());
        if (!CollectionUtils.isEmpty(commodityList)) {
            throw new IllegalStateException("id" + commodityDTO.getId() + "has been taken");
        }
        Commodity commodity = commodityRepository.save(CommodityConverter.converterCommodity(commodityDTO));
        return commodity.getId();
    }

    @Override
    public commodityDTO getCommodityById(Long commodityId) {
        String key = COMMODITY_KEY + commodityId;

        commodityDTO cachedCommodity = (commodityDTO) redisTemplate.opsForValue().get(key);
        if (cachedCommodity != null) {
            log.info("get commodity from cache, id: {}", commodityId);
            return cachedCommodity;
        }

        log.info("get commodity from db, id: {}", commodityId);
        Commodity commodity = commodityRepository.findById(commodityId).orElseThrow(RuntimeException::new);
        commodityDTO dto = CommodityConverter.converterCommodity(commodity);

        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId())
                    .ifPresent(category -> dto.setCategoryName(category.getName()));
        }

        redisTemplate.opsForValue().set(key, dto, 10, TimeUnit.MINUTES);

        return dto;
    }

    @Override
    public commodityDTO updateCommodityById(Long id, String name, String price, Long categoryId, String description, Integer stock) {
        Commodity commodityInDB = commodityRepository.findById(id).orElseThrow(RuntimeException::new);
        if (StringUtils.hasLength(name)) {
            commodityInDB.setName(name);
        }
        if (StringUtils.hasLength(price)) {
            commodityInDB.setPrice(price);
        }
        if (categoryId != null) {
            commodityInDB.setCategoryId(categoryId);
        }
        if (description != null) {
            commodityInDB.setDescription(description);
        }
        if (stock != null) {
            commodityInDB.setStock(stock);
        }
        Commodity commodity = commodityRepository.save(commodityInDB);

        redisTemplate.delete(COMMODITY_KEY + id);
        log.info("update commodity and delete cache, id: {}", id);

        return CommodityConverter.converterCommodity(commodity);
    }

    @Override
    public void deleteCommodityById(Long id) {
        commodityRepository.findById(id).orElseThrow(() -> new IllegalStateException("id:" + id + "is not exist"));
        commodityRepository.deleteById(id);

        redisTemplate.delete(COMMODITY_KEY + id);
        log.info("delete commodity and delete cache, id: {}", id);
    }

    @Override
    public CommodityPageResponse queryCommodities(CommodityPageRequest pageRequest) {
        int page = pageRequest.getPage() != null && pageRequest.getPage() > 0 ? pageRequest.getPage() - 1 : 0;
        int pageSize = pageRequest.getPageSize() != null && pageRequest.getPageSize() > 0 ? pageRequest.getPageSize() : 10;

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Commodity> commodityPage;

        String keyword = pageRequest.getKeyword();
        Long categoryId = pageRequest.getCategoryId();

        if (StringUtils.hasLength(keyword) && categoryId != null) {
            commodityPage = commodityRepository.findByNameContainingAndCategoryId(keyword, categoryId, pageable);
        } else if (StringUtils.hasLength(keyword)) {
            commodityPage = commodityRepository.findByNameContaining(keyword, pageable);
        } else if (categoryId != null) {
            commodityPage = commodityRepository.findByCategoryId(categoryId, pageable);
        } else {
            commodityPage = commodityRepository.findAll(pageable);
        }

        List<commodityDTO> dtoList = commodityPage.getContent().stream()
                .map(commodity -> {
                    commodityDTO dto = CommodityConverter.converterCommodity(commodity);
                    if (dto.getCategoryId() != null) {
                        categoryRepository.findById(dto.getCategoryId())
                                .ifPresent(category -> dto.setCategoryName(category.getName()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return CommodityPageResponse.builder()
                .records(dtoList)
                .total(commodityPage.getTotalElements())
                .page(pageRequest.getPage())
                .pageSize(pageSize)
                .totalPages(commodityPage.getTotalPages())
                .build();
    }
}