package com.example.friendship.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 自己定义返回给前端的包装内容类
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class TeamUserVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3100278890020246950L;

    /**
     * id  唯一标识
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 队伍头像
     */
    private String avatarUrl;

    /**
     * 队伍描述
     */
    private String teamDescription;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 入队用户
     */
    private UserVO createUser;

    /**
     * 当前用户是否已经在当前队伍中
     */
    private boolean hasJoin;

    /**
     * 添加这个队伍目前加入了多少人字段
     */
    private Integer joinNum;
}
