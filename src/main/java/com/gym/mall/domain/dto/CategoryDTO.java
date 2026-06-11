package com.gym.mall.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "商品分类")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "排序权重，数字越小越靠前")
    private Integer sortOrder;
}