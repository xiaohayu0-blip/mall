package com.gym.mall.service.Impl;

import com.gym.mall.Repository.TagGroupRepository;
import com.gym.mall.Repository.TagRepository;
import com.gym.mall.converter.TagConverter;
import com.gym.mall.dao.Tag;
import com.gym.mall.dto.TagDTO;
import com.gym.mall.dto.TagGroupDTO;
import com.gym.mall.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
        return null;
    }

    @Override
    public Map<TagGroupDTO, List<TagDTO>> getAllTags() {
        return Map.of();
    }
}
