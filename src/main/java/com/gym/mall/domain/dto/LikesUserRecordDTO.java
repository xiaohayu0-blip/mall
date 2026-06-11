package com.gym.mall.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "点赞/取消点赞请求")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikesUserRecordDTO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "用户ID（由服务端从 Token 中注入，无需客户端传入）")
    private Long userId;

    @Schema(description = "业务类型ID（如：1=商品）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "businessId 不能为空")
    private Long businessId;

    @Schema(description = "目标条目ID（如商品ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "itemId 不能为空")
    private Long itemId;

    @Schema(description = "true=点赞，false=取消点赞")
    private Boolean likes;
}
