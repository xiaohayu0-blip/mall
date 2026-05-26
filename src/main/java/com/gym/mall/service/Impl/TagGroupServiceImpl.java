package com.gym.mall.service.Impl;

import com.gym.mall.Repository.TagGroupRepository;
import com.gym.mall.domain.dto.TagGroupDTO;
import com.gym.mall.domain.entity.TagGroup;
import com.gym.mall.service.TagGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagGroupServiceImpl implements TagGroupService {

    @Autowired
    private TagGroupRepository tagGroupRepository;

    @Override
    public TagGroupDTO addTagGroup(String tagGroupName) {
        TagGroup tagGroup = new TagGroup();
        tagGroup.setTagGroupName(tagGroupName);
        tagGroup = tagGroupRepository.save(tagGroup);

        return TagGroupDTO.builder()
                .tagGroupId(tagGroup.getId())
                .tagGroupName(tagGroup.getTagGroupName())
                .build();
    }

    @Override
    public TagGroupDTO getTagGroupById(Long id) {
        TagGroup tagGroup = tagGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签组不存在: " + id));

        return TagGroupDTO.builder()
                .tagGroupId(tagGroup.getId())
                .tagGroupName(tagGroup.getTagGroupName())
                .build();
    }

    @Override
    public List<TagGroupDTO> getAllTagGroups() {
        List<TagGroup> tagGroups = tagGroupRepository.findAll();
        return tagGroups.stream()
                .map(group -> TagGroupDTO.builder()
                        .tagGroupId(group.getId())
                        .tagGroupName(group.getTagGroupName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TagGroupDTO updateTagGroup(Long id, String tagGroupName) {
        TagGroup tagGroup = tagGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签组不存在: " + id));

        tagGroup.setTagGroupName(tagGroupName);
        tagGroup = tagGroupRepository.save(tagGroup);

        return TagGroupDTO.builder()
                .tagGroupId(tagGroup.getId())
                .tagGroupName(tagGroup.getTagGroupName())
                .build();
    }

    @Override
    @Transactional
    public void deleteTagGroup(Long id) {
        TagGroup tagGroup = tagGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签组不存在: " + id));

        tagGroupRepository.delete(tagGroup);
    }
}