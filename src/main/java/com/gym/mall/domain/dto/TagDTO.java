package com.gym.mall.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "标签")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {
    @Schema(description = "标签ID")
    private long id;

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标签名称不能为空")
    private String tagName;

    @Schema(description = "所属标签组ID")
    private long tagGroupId;

    @Schema(description = "标签值（用于位运算筛选）")
    private long tagValue;
}
