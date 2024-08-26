package com.example.friendship.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 自定义页面通用包装参数类
 */
@Data
public class PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -1518265174779887562L;
    /**
     * 页面大小，设置默认值，在实际参数为空时可以不用进行多余判断
     */
    protected int pageSize = 10;

    /**
     * 页面编号
     */
    protected int pageNum = 1;
}
