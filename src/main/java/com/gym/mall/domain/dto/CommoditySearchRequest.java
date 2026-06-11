package com.gym.mall.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品全文搜索请求
 *
 * 比 CommodityPageRequest 多了价格区间，专门给 ES 搜索用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommoditySearchRequest {

    private String keyword;

    private Long categoryId;

    /** 最低价格（分） */
    private Long minPrice;

    /** 最高价格（分） */
    private Long maxPrice;

    private Integer page = 1;

    private Integer pageSize = 10;
}
