package com.gym.mall.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "商品信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommodityDTO {
    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品名称不能为空")
    private String name;

    @Schema(description = "价格（单位：分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "9900")
    @NotNull(message = "价格不能为空")
    @Min(value = 0, message = "价格不能为负数")
    private Long price;

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "库存数量", minimum = "0")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @Schema(description = "上架状态：1=上架, 0=下架")
    private Integer status;

    @Schema(description = "标签列表")
    private List<CommodityTagDTO> tags;

    public <E> CommodityDTO(Long commodityId, String commodityName, Long categoryId, String categoryName, ArrayList<E> es) {

    }
}