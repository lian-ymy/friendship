package com.example.friendship.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class UserVO {
    /**
     * id  唯一标识
     */
    private Long id;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 用户个人简介
     */
    private String profile;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态
     */
    private Integer userStatus;
    /**
     * 用户身份 0 普通用户 1 管理员用户
     */
    private Integer userRole;

    /**
     * 星球编号:用于校验登录注册用户是否合法
     */
    private String planetCode;

    /**
     * 标签
     */
    private String tags;
}
