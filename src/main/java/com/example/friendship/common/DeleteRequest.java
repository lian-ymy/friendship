package com.example.friendship.common;

import lombok.Data;

/**
 * 统一的删除数据接口
 */
@Data
public class DeleteRequest {
    /**
     * 要删除的数据对应id
     */
    private Long id;
}
