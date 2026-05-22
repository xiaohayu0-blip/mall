package com.gym.mall.service;

import com.gym.mall.dto.TagDTO;
import com.gym.mall.dto.TagGroupDTO;

import java.util.List;
import java.util.Map;

public interface TagService {

    TagDTO addNewTag(TagDTO tagDTO);

    TagDTO getTagDTOByTagId(long tagId);

    Map<TagGroupDTO, List<TagDTO>> getAllTags();

    List<TagDTO> getTagsByTagGroupId(long tagGroupId);

    void deleteTag(long tagId);

    TagDTO updateTag(long tagId,String tagName);
}
