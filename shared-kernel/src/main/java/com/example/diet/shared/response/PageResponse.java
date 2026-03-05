package com.example.diet.shared.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应包装类
 *
 * @param <T> 记录类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {

    /**
     * 当前页数据
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码（从 1 开始）
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 创建分页响应
     */
    public static <T> PageResponse<T> of(List<T> records, long total, int page, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PageResponse<>(records, total, page, size, totalPages);
    }

    /**
     * 创建空分页响应
     */
    public static <T> PageResponse<T> empty(int page, int size) {
        return new PageResponse<>(Collections.emptyList(), 0, page, size, 0);
    }

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return page < totalPages;
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return page > 1;
    }
}
