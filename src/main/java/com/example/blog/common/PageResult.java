package com.example.blog.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 通用分页返回结构（通用脚手架，任何列表接口都能复用）。
 *
 * 为什么不把 PageHelper 的 PageInfo 直接返回给前端：
 * PageInfo 里混着插件内部字段（如分页导航页码），前端不需要；
 * 自己定义一份"契约稳定"的分页结构，将来换分页实现甚至换 ORM，前端无感知。
 */
@Getter
@AllArgsConstructor
public class PageResult<T> {

    /** 满足条件的总条数 */
    private final long total;

    /** 当前页码（从 1 开始） */
    private final long page;

    /** 每页条数 */
    private final long size;

    /** 总页数，由 total 和 size 推导 */
    private final long pages;

    /** 当前页的数据 */
    private final List<T> list;

    public static <T> PageResult<T> of(long total, long page, long size, List<T> list) {
        long pages = size == 0 ? 0 : (total + size - 1) / size;
        return new PageResult<>(total, page, size, pages, list);
    }
}
