package com.example.friendship.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类，设置通用的返回格式，让前端接收数据时可以对数据进行判断分析
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;

    private T data;

    private String message;

    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse() {
    }

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
    }

    public BaseResponse(ErrorCode errorCode,String description) {
        this(errorCode.getCode(),null,errorCode.getMessage(),description);
    }

    public BaseResponse(ErrorCode errorCode,String message,String description) {
        this(errorCode.getCode(),null,message,description);
    }
}
