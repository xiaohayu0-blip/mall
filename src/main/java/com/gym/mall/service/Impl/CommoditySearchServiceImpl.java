package com.gym.mall.service.Impl;

import com.gym.mall.Repository.CategoryRepository;
import com.gym.mall.Repository.CommodityRepository;
import com.gym.mall.domain.dto.CommodityDTO;
import com.gym.mall.domain.dto.CommodityPageResponse;
import com.gym.mall.domain.dto.CommoditySearchRequest;
import com.gym.mall.domain.entity.Commodity;
import com.gym.mall.service.CommoditySearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommoditySearchServiceImpl implements CommoditySearchService {

    @Autowired
    private CommodityRepository commodityRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    @Lazy
    private CommodityServiceImpl commodityService;

    @Override
    public CommodityPageResponse search(CommoditySearchRequest request) {
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() - 1 : 0;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        String keyword = request.getKeyword();
        Long categoryId = request.getCategoryId();
        Long minPrice = request.getMinPrice();
        Long maxPrice = request.getMaxPrice();

        Page<Commodity> commodityPage = commodityRepository.searchOnSale(
                StringUtils.hasLength(keyword) ? keyword : null,
                categoryId,
                minPrice,
                maxPrice,
                pageable
        );

        List<CommodityDTO> dtoList = commodityPage.getContent().stream()
                .map(c -> {
                    CommodityDTO dto = new CommodityDTO();
                    dto.setId(c.getId());
                    dto.setName(c.getName());
                    dto.setDescription(c.getDescription());
                    dto.setPrice(c.getPrice());
                    dto.setCategoryId(c.getCategoryId());
                    dto.setStock(c.getStock());
                    dto.setStatus(c.getStatus());
                    if (c.getCategoryId() != null) {
                        categoryRepository.findById(c.getCategoryId())
                                .ifPresent(cat -> dto.setCategoryName(cat.getName()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("MySQL 搜索完成，关键词: [{}]，命中: {} 条", keyword, commodityPage.getTotalElements());

        return CommodityPageResponse.builder()
                .records(dtoList)
                .total(commodityPage.getTotalElements())
                .page(request.getPage())
                .pageSize(pageSize)
                .totalPages(commodityPage.getTotalPages())
                .build();
    }

    @Override
    public void upsertDocument(Commodity commodity) {
        // MySQL 作为唯一数据源，无需同步
    }

    @Override
    public void deleteDocument(Long commodityId) {
        // MySQL 作为唯一数据源，无需同步
    }

    @Override
    public void syncAll() {
        // MySQL 作为唯一数据源，无需同步
    }
}
