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
        likesValidator.validateAddNewCommodity(likesUserRecordDTO);
        return Response.newSuccess(likesService.addNewLikesRecord(likesUserRecordDTO));
    }

    @GetMapping("/likes/{businessId}")
    public Response<List<Long>> getMyLikes(@PathVariable Long businessId){
        long userId = BaseContext.getCurrentId();
        return Response.newSuccess(likesService.getMyLikes(businessId, userId));
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
