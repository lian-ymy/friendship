package com.example.friendship.common;

/**
 * 定义返回成功或者返回失败的通用返回类
 */
public class ResultUtils {
    /**
     * 业务执行成功时对应的返回类
     * @param data 对应的实际数据
     * @return 返回通用返回类判断其描述信息是否为成功
     * @param <T> 数据的实际类型
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0,data,"ok");
    }

    /**
     * 失败返回响应
     * @param errorCode 对应的正规错误码或者long类型的错误码
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse(errorCode);
    }

    public static BaseResponse error(ErrorCode errorCode,String description) {
        return new BaseResponse(errorCode,description);
    }

    public static BaseResponse error(ErrorCode errorCode,String message,String description) {
        return new BaseResponse(errorCode,message,description);
    }

    public static BaseResponse error(int errorCode,String message,String description) {
        return new BaseResponse(errorCode,message,description);
    }

}
