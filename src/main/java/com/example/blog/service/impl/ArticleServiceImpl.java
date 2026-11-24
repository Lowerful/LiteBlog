package com.example.blog.service.impl;

import com.example.blog.common.PageResult;
import com.example.blog.common.ResultCode;
import com.example.blog.dto.request.ArticleQueryRequest;
import com.example.blog.dto.request.ArticleSaveRequest;
import com.example.blog.dto.response.ArticleListVO;
import com.example.blog.dto.response.ArticleVO;
import com.example.blog.entity.Article;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.service.ArticleService;
import com.example.blog.util.UserContext;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章业务实现。
 *
 * 权限模型（三个状态码各司其职）：
 * 401 没登录（拦截器管）→ 404 资源不存在 → 403 登录了但不是你的。
 * 判定顺序必须是先 404 再 403：资源不存在时先查存在性，
 * 否则"文章存在但不是你的"会以 403 泄露资源存在性给陌生人。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;

    @Override
    public Long create(ArticleSaveRequest request) {
        Article article = new Article();
        article.setUserId(UserContext.requireUserId());
        article.setTitle(request.getTitle());
        article.setSummary(resolveSummary(request));
        article.setContent(request.getContent());
        article.setStatus(request.getStatus());
        articleMapper.insert(article);
        log.info("article created, id={}, userId={}, status={}", article.getId(), article.getUserId(), article.getStatus());
        return article.getId();
    }

    @Override
    public void update(Long id, ArticleSaveRequest request) {
        Article article = requireOwnedArticle(id);
        article.setTitle(request.getTitle());
        article.setSummary(resolveSummary(request));
        article.setContent(request.getContent());
        article.setStatus(request.getStatus());
        articleMapper.update(article);
    }

    @Override
    public void delete(Long id) {
        requireOwnedArticle(id);
        articleMapper.deleteById(id);
        // article_tag / comment / article_like 上的 ON DELETE CASCADE 会让数据库
        // 自动清理关联行——当初选物理外键换来的便利在这一刻兑现
        log.info("article deleted, id={}, userId={}", id, UserContext.getUserId());
    }

    @Override
    public ArticleVO detail(Long id) {
        ArticleVO vo = articleMapper.selectDetailById(id);
        if (vo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在");
        }
        // 草稿仅作者可见。对陌生人和未登录访客一律返回 404 而不是 403：
        // 不能让未登录的陌生人感知到"这篇草稿存在"
        if ("draft".equals(vo.getStatus())) {
            Long current = UserContext.getUserId();
            if (current == null || !current.equals(vo.getAuthorId())) {
                throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在");
            }
            return vo;
        }
        articleMapper.increaseViews(id);
        return vo;
    }

    @Override
    public PageResult<ArticleListVO> list(ArticleQueryRequest query) {
        // startPage 只对"紧跟其后"的第一条查询生效（靠 ThreadLocal 传标记），
        // 所以 startPage 和 selectList 之间不能插入任何其他查询
        PageHelper.startPage((int) query.getPage(), (int) query.getSize());
        List<ArticleListVO> rows = articleMapper.selectList(query);
        // PageHelper 把返回的 list 换成了 Page 对象，PageInfo 从中取出总数
        PageInfo<ArticleListVO> pageInfo = new PageInfo<>(rows);
        return PageResult.of(pageInfo.getTotal(), query.getPage(), query.getSize(), rows);
    }

    /** 404 → 403 的判定顺序在此集中收口，update 和 delete 共用 */
    private Article requireOwnedArticle(Long id) {
        Article article = articleMapper.findById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在");
        }
        Long current = UserContext.requireUserId();
        if (!current.equals(article.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能操作自己的文章");
        }
        return article;
    }

    /** 摘要留空时自动截取正文前 100 字（先把连续空白压成单个空格，避免截出难看的断行） */
    private String resolveSummary(ArticleSaveRequest request) {
        if (request.getSummary() != null && !request.getSummary().isBlank()) {
            return request.getSummary();
        }
        String plain = request.getContent().replaceAll("\\s+", " ").trim();
        return plain.length() <= 100 ? plain : plain.substring(0, 100);
    }
}
