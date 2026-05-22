package com.gym.mall.converter;

import com.gym.mall.dao.CommodityTag;
import com.gym.mall.dao.Tag;
import com.gym.mall.dto.CommodityTagDTO;

import java.util.ArrayList;
import java.util.List;

public class CommodityTagConverter {
    public static CommodityTagDTO convertToDTO(CommodityTag commodityTag, Tag tag) {
        return CommodityTagDTO.builder()
                .id(commodityTag.getId())
                .commodityId(commodityTag.getCommodityId())
                .tagId(commodityTag.getTagId())
                .tagGroupId(commodityTag.getTagGroupId())
                .tagName(tag != null ? tag.getTagName() : null)
                .build();
    }

    public static List<CommodityTagDTO> convertToList(List<CommodityTag> commodityTags, List<Tag> tags) {
        List<CommodityTagDTO> result = new ArrayList<>();
        for (CommodityTag ct : commodityTags) {
            Tag matchingTag = tags.stream()
                    .filter(t -> t.getId().equals(ct.getTagId()))
                    .findFirst()
                    .orElse(null);
            result.add(convertToDTO(ct, matchingTag));
        }
        return result;
    }
}
