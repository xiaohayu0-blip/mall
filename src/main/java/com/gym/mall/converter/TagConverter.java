package com.gym.mall.converter;

import com.gym.mall.dao.Tag;
import com.gym.mall.dto.TagDTO;

public class TagConverter {
    public static Tag convertToTag(TagDTO tagDTO) {
        return Tag.builder()
                .tagGroupId(tagDTO.getTagGroupId())
                .tagName(tagDTO.getTagName()).build();
    }

    public static TagDTO convertToTagDTO(Tag tag) {
        return TagDTO.builder()
                .id(tag.getId())
                .tagValue(tag.getTagValue())
                .tagName(tag.getTagName())
                .tagGroupId(tag.getTagGroupId())
                .build();
    }
}
