package com.example.friendship.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户请求体
 */
@Data
public class UserLoginRequest implements Serializable {
    private String userAccount;
    private String userPassword;
}