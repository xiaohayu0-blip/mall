package com.gym.mall.controller;

import com.gym.mall.domain.dto.TagDTO;
import com.gym.mall.domain.dto.TagGroupDTO;
import com.gym.mall.validator.Response;
import com.gym.mall.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "标签", description = "商品标签管理")
@RestController
public class TagController {

    @Autowired
    private TagService tagService;

    @Operation(summary = "新增标签")
    @PostMapping("/tag")
    public Response<TagDTO> addNewTag(@Valid @RequestBody TagDTO tagDTO) {
        return Response.newSuccess(tagService.addNewTag(tagDTO));
    }

    @Operation(summary = "根据ID查询标签")
    @GetMapping("/tag/{id}")
    public Response<TagDTO> getTagById(@Parameter(description = "标签ID") @PathVariable Long id) {
        return Response.newSuccess(tagService.getTagDTOByTagId(id));
    }

    @Operation(summary = "查询所有标签（按标签组分组）")
    @GetMapping("/tags")
    public Response<Map<TagGroupDTO, List<TagDTO>>> getAllTags() {
        return Response.newSuccess(tagService.getAllTags());
    }

    @Operation(summary = "查询指定标签组下的所有标签")
    @GetMapping("/tags/group/{groupId}")
    public Response<List<TagDTO>> getTagsByTagGroupId(
            @Parameter(description = "标签组ID") @PathVariable Long groupId) {
        return Response.newSuccess(tagService.getTagsByTagGroupId(groupId));
    }

    @Operation(summary = "更新标签名称")
    @PutMapping("/tag/{id}")
    public Response<TagDTO> updateTag(
            @Parameter(description = "标签ID") @PathVariable Long id,
            @RequestBody String tagName) {
        return Response.newSuccess(tagService.updateTag(id, tagName));
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/tag/{id}")
    public Response<String> deleteTag(@Parameter(description = "标签ID") @PathVariable Long id) {
        tagService.deleteTag(id);
        return Response.newSuccess("删除成功");
    }
}
