package com.gym.mall.converter;


import com.gym.mall.dao.TagGroup;
import com.gym.mall.dto.TagGroupDTO;

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

