package com.gym.mall.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommodityPageResponse {
    private java.util.List<CommodityDTO> records;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;


}