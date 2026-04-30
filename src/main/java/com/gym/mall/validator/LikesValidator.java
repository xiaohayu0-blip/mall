package com.gym.mall.validator;

import com.gym.mall.dto.LikesUserRecordDTO;
import org.springframework.stereotype.Component;

@Component
public class LikesValidator {
    public void validateAddNewCommodity(LikesUserRecordDTO likesUserRecordDTO) {
        if (likesUserRecordDTO.getUserId() == 0L) {
            throw new IllegalArgumentException("userId is empty");
        }
        if (likesUserRecordDTO.getBusinessId() == 0L) {
            throw new IllegalArgumentException("businessId is empty");
        }

        if (likesUserRecordDTO.getItemId() == 0L) {
            throw new IllegalArgumentException("itemId is empty");
        }
    }
}
