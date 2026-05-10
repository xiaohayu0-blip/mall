package com.gym.mall.converter;

import com.gym.mall.dao.LikesUserRecord;
import com.gym.mall.dto.LikesUserRecordDTO;

public class LikesUserRecordConverter {

    public static LikesUserRecord convertToLikesUserRecord(LikesUserRecordDTO likesUserRecordDTO) {
        if(likesUserRecordDTO==null){
            return null;
        }

        return LikesUserRecord.builder()
                .id(likesUserRecordDTO.getId())
                .businessId(likesUserRecordDTO.getBusinessId())
                .itemId(likesUserRecordDTO.getItemId())
                .likes(likesUserRecordDTO.getLikes())
                .userId(likesUserRecordDTO.getUserId())
                .build();
    }
}
