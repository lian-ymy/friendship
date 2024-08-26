package com.example.friendship.model.request;

import lombok.Data;

/**
 * 队伍表
 * @TableName team
 */
@Data
public class TeamJoinRequest {
    /**
     * id  唯一标识
     */
    private Long id;

    /**
     * 队伍密码
     */
    private String teamPassword;
}