package com.gym.mall.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class commodityDTO {
    private Long id;
    private String name;
    private String price;
    private Long categoryId;
    private String categoryName;
    private String description;
    private Integer stock;
    private List<CommodityTagDTO> tags;

    public <E> commodityDTO(Long commodityId, String commodityName, Long categoryId, String categoryName, ArrayList<E> es) {

    }
}