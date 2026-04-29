package com.gym.mall.controller;

import com.gym.mall.dto.TagDTO;
import com.gym.mall.service.Response;
import com.gym.mall.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping("/tag")
    public Response<TagDTO> addNewTag(@RequestBody TagDTO tagDTO){
        return Response.newSuccess(tagService.addNewTag(tagDTO));
    }

}
