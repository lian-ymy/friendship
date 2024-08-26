package com.example.friendship.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class TeamUpdateRequest {
    /**
     * id  唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
