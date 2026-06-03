package com.gym.mall.service.Impl;

import com.gym.mall.Repository.*;
import com.gym.mall.converter.CommodityConverter;
import com.gym.mall.converter.CommodityTagConverter;
import com.gym.mall.domain.entity.Commodity;
import com.gym.mall.domain.entity.CommodityTag;
import com.gym.mall.domain.entity.Tag;
import com.gym.mall.domain.dto.CommodityPageRequest;
import com.gym.mall.domain.dto.CommodityPageResponse;
import com.gym.mall.domain.dto.CommodityTagDTO;
import com.gym.mall.domain.dto.commodityDTO;
import com.gym.mall.service.BloomFilterService;
import com.gym.mall.service.CommodityService;
import com.gym.mall.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.gym.mall.Constants.COMMODITY_KEY;

@Service
@Slf4j
public class CommodityServiceImpl implements CommodityService {

    @Autowired
    private CommodityRepository commodityRepository;

    @Autowired
    private CommodityTagRepository commodityTagRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private BloomFilterService bloomFilterService;

    @Override
    public Long addCommodity(commodityDTO commodityDTO) {
        List<Commodity> commodityList = commodityRepository.findByName(commodityDTO.getName());
        if (!CollectionUtils.isEmpty(commodityList)) {
            throw new IllegalStateException("id" + commodityDTO.getId() + "has been taken");
        }
        Commodity commodity = commodityRepository.save(CommodityConverter.converterCommodity(commodityDTO));
        bloomFilterService.addCommodity(commodity.getId());
        log.info("新增商品并添加到布隆过滤器，商品ID: {}", commodity.getId());

        return commodity.getId();
    }

    @Override
    public commodityDTO getCommodityById(Long commodityId) {
        if (!bloomFilterService.mightContainCommodity(commodityId)) {
            log.warn("商品ID不存在（布隆过滤器拦截）: {}", commodityId);
            throw new RuntimeException("商品不存在: " + commodityId);
        }

        String key = COMMODITY_KEY + commodityId;

        commodityDTO cachedCommodity = (commodityDTO) redisTemplate.opsForValue().get(key);
        if (cachedCommodity != null) {
            log.info("get commodity from cache, id: {}", commodityId);
            return cachedCommodity;
        }

        log.info("get commodity from db, id: {}", commodityId);
        Commodity commodity = commodityRepository.findById(commodityId).orElseThrow(RuntimeException::new);

        // 普通用户看不到下架商品
        String role = BaseContext.getCurrentRole();
        if ("USER".equals(role) && commodity.getStatus() != null && commodity.getStatus() == 0) {
            throw new RuntimeException("商品不存在: " + commodityId);
        }

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
        return updateCommodityById(id, name, price, categoryId, description, stock, null);
    }

    public commodityDTO updateCommodityById(Long id, String name, String price, Long categoryId, String description, Integer stock, Integer status) {
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
        if (status != null) {
            commodityInDB.setStatus(status);
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

        bloomFilterService.initCommodityBloomFilter();
        log.info("删除商品后重新初始化布隆过滤器，商品ID: {}", id);
    }

    @Override
    public CommodityPageResponse queryCommodities(CommodityPageRequest pageRequest) {
        int page = pageRequest.getPage() != null && pageRequest.getPage() > 0 ? pageRequest.getPage() - 1 : 0;
        int pageSize = pageRequest.getPageSize() != null && pageRequest.getPageSize() > 0 ? pageRequest.getPageSize() : 10;

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Commodity> commodityPage;

        String keyword = pageRequest.getKeyword();
        Long categoryId = pageRequest.getCategoryId();
        String role = BaseContext.getCurrentRole();

        // 普通用户只能看到上架商品（status=1），管理员可以看到全部
        boolean isUser = "USER".equals(role);

        if (isUser) {
            if (StringUtils.hasLength(keyword) && categoryId != null) {
                commodityPage = commodityRepository.findByNameContainingAndCategoryIdAndStatus(keyword, categoryId, 1, pageable);
            } else if (StringUtils.hasLength(keyword)) {
                commodityPage = commodityRepository.findByNameContainingAndStatus(keyword, 1, pageable);
            } else if (categoryId != null) {
                commodityPage = commodityRepository.findByCategoryIdAndStatus(categoryId, 1, pageable);
            } else {
                commodityPage = commodityRepository.findByStatus(1, pageable);
            }
        } else {
            if (StringUtils.hasLength(keyword) && categoryId != null) {
                commodityPage = commodityRepository.findByNameContainingAndCategoryId(keyword, categoryId, pageable);
            } else if (StringUtils.hasLength(keyword)) {
                commodityPage = commodityRepository.findByNameContaining(keyword, pageable);
            } else if (categoryId != null) {
                commodityPage = commodityRepository.findByCategoryId(categoryId, pageable);
            } else {
                commodityPage = commodityRepository.findAll(pageable);
            }
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
    @Override
    @Transactional
    //声明式事务管理
    //保证这个方法里的所有数据库操作要么全部成功，要么全部失败回滚
    public commodityDTO bindTagsToCommodity(Long commodityId,List<Long> tagIds){
        Commodity commodity = commodityRepository.findById(commodityId)
                .orElseThrow(()->new RuntimeException("商品不存在:"+commodityId));

        for(Long tagId:tagIds){
            Tag tag=tagRepository.findById(tagId)
                    .orElseThrow(()->new RuntimeException("标签不存在:"+tagId));

            boolean exits=commodityTagRepository.existsByCommodityIdAndTagId(commodityId,tagId);
            if(!exits){
                CommodityTag commodityTag=CommodityTag.builder()
                        .commodityId(commodityId)
                        .tagId(tagId)
                        .tagGroupId(tag.getTagGroupId())
                        .build();
                commodityTagRepository.save(commodityTag);
                log.info("绑定标签到商品,commodityId:{},tagId:{}",commodityId,tagId);
            }
        }

        redisTemplate.delete(COMMODITY_KEY+commodityId);
        //删除 Redis 中该商品的缓存，防止返回旧数据。

        return getCommodityById(commodityId);
    }

    @Override
    public void unbindTagFromCommodity(Long commodityId, Long tagId) {
        Commodity commodity = commodityRepository.findById(commodityId)
                .orElseThrow(() -> new RuntimeException("商品不存在:" + commodityId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在:" + tagId));

        commodityTagRepository.deleteByCommodityIdAndTagId(commodityId, tagId);
        log.info("解绑标签,commodityId:{},tagId:{}", commodityId, tagId);

        redisTemplate.delete(COMMODITY_KEY + commodityId);

    }

    @Override
    public List<commodityDTO> getCommoditiesByTagId(Long tagId) {

        List<CommodityTagView> views = commodityRepository.findCommoditiesWithTags(tagId);

        Map<Long, commodityDTO> dtoMap = new HashMap<>();

        for (CommodityTagView view : views) {

            commodityDTO dto = dtoMap.computeIfAbsent(
                    view.getCommodityId(),
                    id -> new commodityDTO(
                            view.getCommodityId(),
                            view.getCommodityName(),
                            view.getCategoryId(),
                            view.getCategoryName(),
                            new ArrayList<>()
                    )
            );

            if (view.getTagId() != null) {
                dto.getTags().add(new CommodityTagDTO(view.getTagId(), view.getTagName()));
            }
        }

        return new ArrayList<>(dtoMap.values());
    }

    @Override
    public CommodityPageResponse queryCommoditiesByTags(List<Long> tagIds, Integer page, Integer pageSize) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new CommodityPageResponse();
        }

        List<Long> commodityIds = commodityTagRepository.findCommodityIdsByTagIds(tagIds);

        if (commodityIds.isEmpty()) {
            return new CommodityPageResponse();
        }

        int pageNum = page != null && page > 0 ? page - 1 : 0;
        int size = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(pageNum, size, Sort.by(Sort.Direction.DESC, "id"));

        // 普通用户只看到上架商品
        String role = BaseContext.getCurrentRole();
        boolean isUser = "USER".equals(role);
        Page<Commodity> commodityPage;
        if (isUser) {
            commodityPage = commodityRepository.findAllByIdInAndStatus(commodityIds, pageable);
        } else {
            commodityPage = commodityRepository.findAllByIdIn(commodityIds, pageable);
        }

        List<commodityDTO> dtoList = commodityPage.getContent().stream()
                .map(commodity -> {
                    commodityDTO dto = CommodityConverter.converterCommodity(commodity);
                    if (dto.getCategoryId() != null) {
                        categoryRepository.findById(dto.getCategoryId())
                                .ifPresent(category -> dto.setCategoryName(category.getName()));
                    }

                    List<CommodityTag> tags = commodityTagRepository.findByCommodityId(commodity.getId());
                    if (!tags.isEmpty()) {
                        List<Long> existingTagIds = tags.stream()
                                .map(CommodityTag::getTagId)
                                .collect(Collectors.toList());
                        List<Tag> tagList = tagRepository.findByIdIn(existingTagIds);
                        List<CommodityTagDTO> tagDTOs = CommodityTagConverter.convertToList(tags, tagList);
                        dto.setTags(tagDTOs);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        return CommodityPageResponse.builder()
                .records(dtoList)
                .total(commodityPage.getTotalElements())
                .page(page)
                .pageSize(size)
                .totalPages(commodityPage.getTotalPages())
                .build();
    }
}