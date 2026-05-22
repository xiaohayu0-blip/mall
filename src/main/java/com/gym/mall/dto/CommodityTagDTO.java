package com.gym.mall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommodityTagDTO {
    private Long id;
    private Long commodityId;
    private Long tagId;
    private String tagName;
    private Long tagGroupId;

    public CommodityTagDTO(Long tagId, String tagName) {

    }
}
