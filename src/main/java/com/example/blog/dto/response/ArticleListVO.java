package com.example.blog.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 文章列表条目。刻意不带 content——正文可能几万字，
 * 列表页一页 10 条就是把几十万字拉给浏览器，纯浪费带宽。
 * "列表 VO 和详情 VO 分开"是接口设计的默认姿势。
 */
@Getter
@Builder
public class ArticleListVO {

    private final Long id;
    private final String title;
    private final String summary;
    private final Long views;
    private final Long authorId;
    private final String authorNickname;
    private final LocalDateTime createdAt;
}
