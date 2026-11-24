package com.example.blog.controller;

import com.example.blog.common.PageResult;
import com.example.blog.common.Result;
import com.example.blog.dto.request.ArticleQueryRequest;
import com.example.blog.dto.request.ArticleSaveRequest;
import com.example.blog.dto.response.ArticleListVO;
import com.example.blog.dto.response.ArticleVO;
import com.example.blog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章接口——RESTful 语义第一次完整出场：
 *
 * POST   /api/articles       创建
 * PUT    /api/articles/{id}  全量更新（@PathVariable 首次实战）
 * DELETE /api/articles/{id}  删除
 * GET    /api/articles/{id}  详情（公开，无 token 也可访问）
 * GET    /api/articles       分页列表（公开，@RequestParam 式的查询参数首次实战）
 *
 * 注意：写操作不需要在参数里传 userId——它来自 UserContext（第 3 步的成果）。
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    public Result<Long> create(@Valid @RequestBody ArticleSaveRequest request) {
        return Result.ok(articleService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleSaveRequest request) {
        articleService.update(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.ok(articleService.detail(id));
    }

    @GetMapping
    public Result<PageResult<ArticleListVO>> list(@Valid ArticleQueryRequest query) {
        // GET 没有请求体：query 参数由 Spring 按 ?page=1&size=10 绑定到对象字段上
        return Result.ok(articleService.list(query));
    }
}
