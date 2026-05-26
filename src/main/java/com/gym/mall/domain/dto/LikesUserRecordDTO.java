package com.gym.mall.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikesUserRecordDTO {

    private Long id;

    private Long userId;

    private Long businessId;

    private Long itemId;

    private Boolean likes;
}
