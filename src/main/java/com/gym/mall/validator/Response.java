package com.gym.mall.validator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Response<T> {
    private T data;
    private boolean success;
    private String message;

    public static <K>Response<K> newSuccess(K data){
        Response<K> response=new Response<>();
        response.setData(data);
        response.setSuccess(true);
        return response;
    }

    public static Response<String> newFail(String errorMsg){
        Response<String> response=new Response<>();
        response.setSuccess(false);
        response.setMessage(errorMsg);
        return response;
    }


}
