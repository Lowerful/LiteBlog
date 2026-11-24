package com.example.blog.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 文章详情。由 Mapper 连表查询直接产出（含作者昵称），Service 不做二次拼装。
 */
@Getter
@Builder
public class ArticleVO {

    private final Long id;
    private final String title;
    private final String summary;
    private final String content;
    private final String status;
    private final Long views;
    private final Long authorId;
    private final String authorNickname;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
