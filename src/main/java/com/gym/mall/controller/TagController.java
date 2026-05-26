package com.gym.mall.controller;

import com.gym.mall.domain.dto.TagDTO;
import com.gym.mall.domain.dto.TagGroupDTO;
import com.gym.mall.validator.Response;
import com.gym.mall.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping("/tag")
    public Response<TagDTO> addNewTag(@RequestBody TagDTO tagDTO){
        return Response.newSuccess(tagService.addNewTag(tagDTO));
    }
    @GetMapping(("/tag/{id}"))
    public Response<TagDTO> getTagById(@PathVariable Long id){
        return Response.newSuccess(tagService.getTagDTOByTagId(id));
    }

    @GetMapping(("/tags"))
    public Response<Map<TagGroupDTO, List<TagDTO>>> getAllTags(){
        return Response.newSuccess(tagService.getAllTags());
    }

    @GetMapping(("/tags/group/{groupId}"))
    public Response<List<TagDTO>> getTagsByTagGroupId(@PathVariable Long groupId){
        return Response.newSuccess(tagService.getTagsByTagGroupId(groupId));
    }

    @PutMapping(("/tag/{id}"))
    public Response<TagDTO> updateTag(@PathVariable Long id, @RequestBody String tagName){
        return Response.newSuccess(tagService.updateTag(id,tagName));
    }

    @DeleteMapping(("/tag/{id}"))
    public Response< String> deleteTag(@PathVariable Long id){
        try {
            tagService.deleteTag(id);
            return Response.newSuccess("删除成功");
        } catch (Exception e) {
            return Response.newFail("删除失败:"+e.getMessage());
        }
    }
}
