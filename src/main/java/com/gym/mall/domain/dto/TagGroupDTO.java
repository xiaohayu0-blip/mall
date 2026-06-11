package com.gym.mall.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "标签组")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagGroupDTO {
    @Schema(description = "标签组ID")
    private long tagGroupId;

    @Schema(description = "标签组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标签组名称不能为空")
    private String tagGroupName;
}
