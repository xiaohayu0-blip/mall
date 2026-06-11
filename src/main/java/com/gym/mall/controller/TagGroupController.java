package com.gym.mall.controller;

import com.gym.mall.domain.dto.TagGroupDTO;
import com.gym.mall.service.TagGroupService;
import com.gym.mall.validator.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "标签组", description = "标签组管理")
@RestController
@RequestMapping("/tag-group")
public class TagGroupController {

    @Autowired
    private TagGroupService tagGroupService;

    @Operation(summary = "新增标签组")
    @PostMapping
    public Response<TagGroupDTO> addTagGroup(@Valid @RequestBody TagGroupDTO tagGroupDTO) {
        TagGroupDTO result = tagGroupService.addTagGroup(tagGroupDTO.getTagGroupName());
        return Response.newSuccess(result);
    }

    @Operation(summary = "根据ID查询标签组")
    @GetMapping("/{id}")
    public Response<TagGroupDTO> getTagGroupById(
            @Parameter(description = "标签组ID") @PathVariable Long id) {
        TagGroupDTO result = tagGroupService.getTagGroupById(id);
        return Response.newSuccess(result);
    }

    @Operation(summary = "查询所有标签组")
    @GetMapping
    public Response<List<TagGroupDTO>> getAllTagGroups() {
        List<TagGroupDTO> result = tagGroupService.getAllTagGroups();
        return Response.newSuccess(result);
    }

    @Operation(summary = "更新标签组名称")
    @PutMapping("/{id}")
    public Response<TagGroupDTO> updateTagGroup(
            @Parameter(description = "标签组ID") @PathVariable Long id,
            @Valid @RequestBody TagGroupDTO tagGroupDTO) {
        TagGroupDTO result = tagGroupService.updateTagGroup(id, tagGroupDTO.getTagGroupName());
        return Response.newSuccess(result);
    }

    @Operation(summary = "删除标签组")
    @DeleteMapping("/{id}")
    public Response<String> deleteTagGroup(
            @Parameter(description = "标签组ID") @PathVariable Long id) {
        tagGroupService.deleteTagGroup(id);
        return Response.newSuccess("删除成功");
    }
}