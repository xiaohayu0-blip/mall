package com.gym.mall.controller;

import com.gym.mall.domain.dto.LikesUserRecordDTO;
import com.gym.mall.service.LikesService;
import com.gym.mall.utils.BaseContext;
import com.gym.mall.validator.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "点赞", description = "商品或内容的点赞/取消点赞")
@RestController
public class LikesController {

    @Autowired
    private LikesService likesService;

    @Operation(summary = "点赞或取消点赞")
    @PostMapping("/likes")
    public Response<Boolean> addLikeItem(@Valid @RequestBody LikesUserRecordDTO likesUserRecordDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        likesUserRecordDTO.setUserId(currentUserId);
        return Response.newSuccess(likesService.addNewLikesRecord(likesUserRecordDTO));
    }

    @Operation(summary = "查询我点赞的条目ID列表")
    @GetMapping("/likes/{businessId}")
    public Response<List<Long>> getMyLikes(
            @Parameter(description = "业务类型ID") @PathVariable Long businessId) {
        long userId = BaseContext.getCurrentId();
        return Response.newSuccess(likesService.getMyLikes(userId, businessId));
    }

    @Operation(summary = "查询指定条目的点赞总数")
    @GetMapping("/likes/count")
    public Response<Long> getItemLikesCount(
            @Parameter(description = "业务类型ID") @RequestParam Long businessId,
            @Parameter(description = "条目ID") @RequestParam Long itemId) {
        return Response.newSuccess(likesService.getItemLikesCount(businessId, itemId));
    }

    @Operation(summary = "查询指定用户是否点赞了某条目")
    @GetMapping("/likes/status")
    public Response<Boolean> hasLiked(
            @Parameter(description = "用户ID") @RequestParam long userId,
            @Parameter(description = "业务类型ID") @RequestParam long businessId,
            @Parameter(description = "条目ID") @RequestParam long itemId) {
        return Response.newSuccess(likesService.hasLiked(userId, businessId, itemId));
    }
}
