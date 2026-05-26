package com.gym.mall.converter;


import com.gym.mall.domain.entity.TagGroup;
import com.gym.mall.domain.dto.TagGroupDTO;

public class TagGroupConverter {

    public static TagGroupDTO converToTagGroupDTO(TagGroup tagGroup) {
        if (tagGroup == null) {
            return null;
        }

        return TagGroupDTO.builder()
                .tagGroupId(tagGroup.getId())
                .tagGroupName(tagGroup.getTagGroupName()).build();
    }
}

