package com.gym.mall.service;

import com.gym.mall.domain.dto.TagGroupDTO;

import java.util.List;

public interface TagGroupService {

    TagGroupDTO addTagGroup(String tagGroupName);

    TagGroupDTO getTagGroupById(Long id);

    List<TagGroupDTO> getAllTagGroups();

    TagGroupDTO updateTagGroup(Long id, String tagGroupName);

    void deleteTagGroup(Long id);
}
