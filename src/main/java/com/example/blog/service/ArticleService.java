package com.example.blog.service;

import com.example.blog.common.PageResult;
import com.example.blog.dto.request.ArticleQueryRequest;
import com.example.blog.dto.request.ArticleSaveRequest;
import com.example.blog.dto.response.ArticleListVO;
import com.example.blog.dto.response.ArticleVO;

/**
 * 文章业务：发布、编辑、删除、详情、分页列表。
 */
public interface ArticleService {

    /** 发布/存草稿，返回新文章 id */
    Long create(ArticleSaveRequest request);

    /** 全量更新，仅作者可操作 */
    void update(Long id, ArticleSaveRequest request);

    /** 删除，仅作者可操作；关联数据由数据库级联清理 */
    void delete(Long id);

    /** 详情：已发布文章公开可见，草稿仅作者可见 */
    ArticleVO detail(Long id);

    /** 分页列表：仅含已发布文章，支持关键字与作者筛选 */
    PageResult<ArticleListVO> list(ArticleQueryRequest query);
}
