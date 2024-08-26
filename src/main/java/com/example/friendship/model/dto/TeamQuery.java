package com.example.friendship.model.dto;

import com.example.friendship.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 前端传递过来的队伍查询参数封装类
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class TeamQuery extends PageRequest {
        @Serial
        private static final long serialVersionUID = -6299011872759671941L;
        /**
         * id  唯一标识
         */
        private Long id;

        /**
         * idList  加入的队伍id集合
         */
        private List<Long> idList;

        /**
         * 队伍名称
         */
        private String teamName;

        /**
         * 名称和描述都可以查询匹配的字段
         */
        private String searchText;

        /**
         * 队伍描述
         */
        private String teamDescription;

        /**
         * 队伍人数
         */
        private Long maxNum;

        /**
         * 创建人用户Id
         */
        private Long userId;

        /**
         * 队伍状态：0公开 1私密 2加密
         */
        private Integer teamStatus;
}
