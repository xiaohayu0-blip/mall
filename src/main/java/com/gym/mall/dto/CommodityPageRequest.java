package com.gym.mall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommodityPageRequest {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String keyword;
    private Long categoryId;

}