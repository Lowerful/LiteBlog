package com.example.blog.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 文章列表的查询参数（GET 请求，Spring 按 query string 自动绑定到字段）。
 *
 * 注意 size 有上限：防止有人传 size=1000000 一次拉全表——
 * 分页上限是"成本防线"，不只是体验参数。
 */
@Data
public class ArticleQueryRequest {

    @Min(value = 1, message = "页码最小为 1")
    private long page = 1;

    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = 50, message = "每页条数最大为 50")
    private long size = 10;

    /** 关键字：按标题/摘要模糊搜索，可为空 */
    private String keyword;

    /** 按作者筛选，可为空 */
    private Long authorId;
}
