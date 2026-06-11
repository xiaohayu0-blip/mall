package com.gym.mall.service;

import com.gym.mall.domain.dto.CommodityPageResponse;
import com.gym.mall.domain.dto.CommoditySearchRequest;
import com.gym.mall.domain.entity.Commodity;

public interface CommoditySearchService {

    /**
     * 全文搜索商品
     *
     * 支持：关键词多字段匹配（name/description）、分类过滤、价格区间过滤、上架过滤
     * 降级：ES 不可用时自动回落到 MySQL LIKE 查询
     */
    CommodityPageResponse search(CommoditySearchRequest request);

    /**
     * 新增或更新 ES 文档，直接接收实体，由实现负责转换
     */
    void upsertDocument(Commodity commodity);

    /**
     * 删除 ES 文档（DB 删除后调用）
     */
    void deleteDocument(Long commodityId);

    /**
     * 全量同步 MySQL -> ES（启动时调用）
     */
    void syncAll();
}
