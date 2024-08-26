package com.example.friendship.model.request;

import lombok.Data;

import java.util.Date;

/**
 * 自定义展示到前端的队伍类
 *
 * 删除前端不需要传递创建的字段，使得前端页面简洁化
 */
@Data
public class TeamAddRequest {

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 队伍描述
     */
    private String teamDescription;

    /**
     * 队伍头像
     */
    private String avatarUrl;

    /**
     * 队伍人数
     */
    private Long maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建人用户Id
     */
    private Long userId;

    /**
     * 队伍状态：0公开 1私密 2加密
     */
    private Integer teamStatus;

    /**
     * 队伍密码
     */
    private String teamPassword;
}
