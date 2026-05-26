package com.gym.mall.controller;

import com.gym.mall.domain.dto.TagGroupDTO;
import com.gym.mall.service.TagGroupService;
import com.gym.mall.validator.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag-group")
public class TagGroupController {

    @Autowired
    private TagGroupService tagGroupService;

    @PostMapping
    public Response<TagGroupDTO> addTagGroup(@RequestBody TagGroupDTO tagGroupDTO) {
        try {
            TagGroupDTO result = tagGroupService.addTagGroup(tagGroupDTO.getTagGroupName());
            return Response.newSuccess(result);
        } catch (Exception e) {
            return Response.newFail("创建标签组失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Response<TagGroupDTO> getTagGroupById(@PathVariable Long id) {
        try {
            TagGroupDTO result = tagGroupService.getTagGroupById(id);
            return Response.newSuccess(result);
        } catch (Exception e) {
            return Response.newFail("查询标签组失败: " + e.getMessage());
        }
    }

    @GetMapping
    public Response<List<TagGroupDTO>> getAllTagGroups() {
        try {
            List<TagGroupDTO> result = tagGroupService.getAllTagGroups();
            return Response.newSuccess(result);
        } catch (Exception e) {
            return Response.newFail("查询标签组列表失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Response<TagGroupDTO> updateTagGroup(@PathVariable Long id,
                                                @RequestBody TagGroupDTO tagGroupDTO) {
        try {
            TagGroupDTO result = tagGroupService.updateTagGroup(id, tagGroupDTO.getTagGroupName());
            return Response.newSuccess(result);
        } catch (Exception e) {
            return Response.newFail("更新标签组失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Response<String> deleteTagGroup(@PathVariable Long id) {
        try {
            tagGroupService.deleteTagGroup(id);
            return Response.newSuccess("删除成功");
        } catch (Exception e) {
            return Response.newFail("删除失败: " + e.getMessage());
        }
    }
}