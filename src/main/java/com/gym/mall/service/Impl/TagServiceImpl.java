package com.gym.mall.service.Impl;

import com.gym.mall.Repository.TagGroupRepository;
import com.gym.mall.Repository.TagRepository;
import com.gym.mall.converter.TagConverter;
import com.gym.mall.dao.Tag;
import com.gym.mall.dao.TagGroup;
import com.gym.mall.dto.TagDTO;
import com.gym.mall.dto.TagGroupDTO;
import com.gym.mall.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagGroupRepository tagGroupRepository;

    @Override
    public TagDTO addNewTag(TagDTO tagDTO) {
        Tag tag= TagConverter.convertToTag(tagDTO);

        long maxTagValue=tagRepository.findMaxTagInGroup(tagDTO.getTagGroupId()).orElse(0L);
        if(maxTagValue>=Long.MAX_VALUE){
            throw new UnsupportedOperationException("tagGroupId:" + tagDTO.getTagGroupId() + " 下tag数量已满");
        }
        tag.setTagValue(maxTagValue == 0L ? 1L : maxTagValue << 1);
        tag = tagRepository.save(tag);
        return TagConverter.convertToTagDTO(tag);
    }

    @Override
    public TagDTO getTagDTOByTagId(long tagId) {
        Tag tag=tagRepository.findById(tagId)
                .orElseThrow(()->new RuntimeException("标签不存在:"+tagId));

        return TagConverter.convertToTagDTO(tag);
    }

    @Override
    public Map<TagGroupDTO, List<TagDTO>> getAllTags() {
        List<TagGroup> tagGroups=tagGroupRepository.findAll();
        List<Tag> allTags=tagRepository.findAll();

        Map<TagGroupDTO, List<TagDTO>> result=new HashMap<>();

        for (TagGroup group : tagGroups) {
            TagGroupDTO groupDTO = TagGroupDTO.builder()
                    .tagGroupId(group.getId())
                    .tagGroupName(group.getTagGroupName())
                    .build();

            List<TagDTO> tagsInGroup = allTags.stream()
                    .filter(tag -> tag.getTagGroupId() == group.getId())
                    .map(TagConverter::convertToTagDTO)
                    .collect(Collectors.toList());
            result.put(groupDTO, tagsInGroup);
        }
        return result;
    }

    @Override
    public List<TagDTO> getTagsByTagGroupId(long tagGroupId) {
        List<Tag> tags=tagRepository.findByTagGroupId(tagGroupId);
        return tags.stream()
                .map(TagConverter::convertToTagDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTag(long tagId) {
        Tag tag=tagRepository.findById(tagId)
                .orElseThrow(()->new RuntimeException("标签不存在:"+tagId));
        tagRepository.delete(tag);
    }

    @Override
    public TagDTO updateTag(long tagId, String tagName) {
        Tag tag=tagRepository.findById(tagId)
                .orElseThrow(()->new RuntimeException("标签不存在:"+tagId));
        tag.setTagName(tagName);
        tag=tagRepository.save(tag);
        return TagConverter.convertToTagDTO(tag);
    }
}
