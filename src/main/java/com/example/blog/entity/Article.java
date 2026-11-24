package com.example.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体，与 article 表一一对应。
 * 实体只做数据搬运，业务判断（状态流转、归属校验）都在 Service 层。
 */
@Data
public class Article {

    private Long id;

    /** 作者，对应 user.id */
    private Long userId;

    private String title;

    /** 摘要：列表页展示，正文可能几万字，列表绝不查正文 */
    private String summary;

    private String content;

    /** 状态：draft（草稿，仅作者可见）/ published（已发布） */
    private String status;

    /** 阅读数，详情页访问时原子自增 */
    private Long views;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
