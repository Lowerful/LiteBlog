package com.example.blog.service;

import com.example.blog.common.ResultCode;
import com.example.blog.dto.request.ArticleSaveRequest;
import com.example.blog.dto.response.ArticleVO;
import com.example.blog.entity.Article;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.service.impl.ArticleServiceImpl;
import com.example.blog.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 文章业务单元测试：聚焦本步新增的三类业务规则——
 * 归属校验的 404/403 顺序、草稿可见性、摘要自动生成。
 * Mapper 打桩，不起容器。分页属于 PageHelper 插件行为，交给端到端验证。
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleMapper articleMapper;

    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        articleService = new ArticleServiceImpl(articleMapper);
        UserContext.setUserId(1L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private ArticleSaveRequest request(String title, String content, String summary, String status) {
        ArticleSaveRequest request = new ArticleSaveRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setSummary(summary);
        request.setStatus(status);
        return request;
    }

    private Article ownedArticle(Long ownerId) {
        Article article = new Article();
        article.setId(1L);
        article.setUserId(ownerId);
        article.setTitle("旧标题");
        return article;
    }

    @Test
    @DisplayName("发布文章：摘要留空时自动截取正文前 100 字")
    void createShouldGenerateSummaryWhenBlank() {
        String content = "正".repeat(300);

        articleService.create(request("标题", content, " ", "published"));

        ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        verify(articleMapper).insert(captor.capture());
        Article inserted = captor.getValue();
        assertEquals(1L, inserted.getUserId(), "作者取自 UserContext，而不是请求参数");
        assertEquals(100, inserted.getSummary().length(), "自动摘要应为正文前 100 字");
    }

    @Test
    @DisplayName("发布文章：显式提供的摘要被保留")
    void createShouldKeepProvidedSummary() {
        articleService.create(request("标题", "正文", "我的摘要", "published"));

        ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        verify(articleMapper).insert(captor.capture());
        assertEquals("我的摘要", captor.getValue().getSummary());
    }

    @Test
    @DisplayName("编辑文章：文章不存在返回 404，且不执行更新")
    void updateShouldThrowNotFoundWhenArticleMissing() {
        when(articleMapper.findById(9L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> articleService.update(9L, request("新标题", "新正文", null, "published")));

        assertEquals(ResultCode.NOT_FOUND.getCode(), e.getResultCode().getCode());
        verify(articleMapper, never()).update(any());
    }

    @Test
    @DisplayName("编辑文章：不是作者返回 403，且不执行更新")
    void updateShouldThrowForbiddenWhenNotOwner() {
        when(articleMapper.findById(1L)).thenReturn(ownedArticle(2L));

        BusinessException e = assertThrows(BusinessException.class,
                () -> articleService.update(1L, request("新标题", "新正文", null, "published")));

        assertEquals(ResultCode.FORBIDDEN.getCode(), e.getResultCode().getCode());
        verify(articleMapper, never()).update(any());
    }

    @Test
    @DisplayName("编辑文章：作者本人可以更新")
    void updateShouldSucceedForOwner() {
        when(articleMapper.findById(1L)).thenReturn(ownedArticle(1L));

        articleService.update(1L, request("新标题", "新正文", null, "published"));

        verify(articleMapper).update(any(Article.class));
    }

    @Test
    @DisplayName("删除文章：作者本人可以删除")
    void deleteShouldSucceedForOwner() {
        when(articleMapper.findById(1L)).thenReturn(ownedArticle(1L));

        articleService.delete(1L);

        verify(articleMapper).deleteById(1L);
    }

    @Test
    @DisplayName("草稿可见性：他人的草稿对登录用户也返回 404（不能泄露草稿存在性）")
    void detailShouldHideDraftFromStranger() {
        ArticleVO draft = ArticleVO.builder().id(1L).status("draft").authorId(2L).build();
        when(articleMapper.selectDetailById(1L)).thenReturn(draft);

        BusinessException e = assertThrows(BusinessException.class, () -> articleService.detail(1L));

        assertEquals(ResultCode.NOT_FOUND.getCode(), e.getResultCode().getCode());
        verify(articleMapper, never()).increaseViews(any());
    }

    @Test
    @DisplayName("草稿可见性：作者本人可以看到自己的草稿")
    void detailShouldAllowDraftForAuthor() {
        ArticleVO draft = ArticleVO.builder().id(1L).status("draft").authorId(1L).content("草稿正文").build();
        when(articleMapper.selectDetailById(1L)).thenReturn(draft);

        ArticleVO vo = articleService.detail(1L);

        assertNotNull(vo);
        assertEquals("草稿正文", vo.getContent());
        verify(articleMapper, never()).increaseViews(any());
    }

    @Test
    @DisplayName("已发布文章详情：任何人可见，且阅读数自增")
    void detailShouldIncreaseViewsForPublished() {
        ArticleVO published = ArticleVO.builder().id(1L).status("published").authorId(2L).build();
        when(articleMapper.selectDetailById(1L)).thenReturn(published);

        ArticleVO vo = articleService.detail(1L);

        assertNotNull(vo);
        verify(articleMapper).increaseViews(1L);
    }
}
