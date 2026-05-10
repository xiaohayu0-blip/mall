package com.gym.mall.controller;

import com.gym.mall.dto.LikesUserRecordDTO;
import com.gym.mall.service.LikesService;
import com.gym.mall.utils.BaseContext;
import com.gym.mall.validator.LikesValidator;
import com.gym.mall.validator.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LikesController {

    @Autowired
    private LikesService likesService;

    @Autowired
    private LikesValidator likesValidator;

    @PostMapping("/likes")
    public Response<Boolean> addLikeItem(@RequestBody LikesUserRecordDTO likesUserRecordDTO){
        // 【核心安全改动】：不再相信前端传来的 userId，而是直接从 ThreadLocal (BaseContext) 中获取
        // 这里的 ID 是保安（拦截器）从加密的 Token 中解析出来的，绝对真实可靠
        Long currentUserId = BaseContext.getCurrentId();
        likesUserRecordDTO.setUserId(currentUserId);
        
        // 校验参数合法性
        likesValidator.validateAddNewCommodity(likesUserRecordDTO);
        
        // 调用 Service 执行异步点赞逻辑
        return Response.newSuccess(likesService.addNewLikesRecord(likesUserRecordDTO));
    }

    /**
     * 获取当前登录用户的所有点赞记录
     */
    @GetMapping("/likes/{businessId}")
    public Response<List<Long>> getMyLikes(@PathVariable Long businessId){
        // 同样从 ThreadLocal 中获取当前用户 ID
        long userId = BaseContext.getCurrentId();
        return Response.newSuccess(likesService.getMyLikes(userId, businessId));
    }

    @GetMapping("/likes/count")
    public Response<Long> getItemLikesCount(@RequestParam Long businessId, @RequestParam Long itemId){
        return Response.newSuccess(likesService.getItemLikesCount(businessId, itemId));
    }

    @GetMapping("/likes/status")
    public Response<Boolean> hasLiked(@RequestParam long userId, @RequestParam long businessId, @RequestParam long itemId){
        return Response.newSuccess(likesService.hasLiked(userId, businessId, itemId));
    }
}
