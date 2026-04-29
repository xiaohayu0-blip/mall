package com.gym.mall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {
    private long id;

    private String tagName;

    private long tagGroupId;

    private long tagValue;

}
