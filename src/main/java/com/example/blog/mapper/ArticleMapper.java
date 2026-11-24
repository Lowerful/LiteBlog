package com.example.blog.mapper;

import com.example.blog.dto.request.ArticleQueryRequest;
import com.example.blog.dto.response.ArticleListVO;
import com.example.blog.dto.response.ArticleVO;
import com.example.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章数据访问。
 *
 * 一个值得注意的设计：查询直接返回 VO（连表取作者昵称），
 * 这叫"查询导向"设计——查出来就是接口要的形状，Service 不用二次拼装；
 * 写操作（insert/update/delete）仍然操作实体。
 * 两种风格并存是 MyBatis 项目的常态：读用 VO，写用实体。
 */
@Mapper
public interface ArticleMapper {

    int insert(Article article);

    /** 按 id 查实体（不含作者信息），用于更新/删除前的归属校验 */
    Article findById(@Param("id") Long id);

    /** 详情查询：join user 表取作者昵称，直接产出详情 VO */
    ArticleVO selectDetailById(@Param("id") Long id);

    /** 条件 + 排序查询，分页由 PageHelper 插件在 SQL 外层完成 */
    List<ArticleListVO> selectList(@Param("query") ArticleQueryRequest query);

    int update(Article article);

    int deleteById(@Param("id") Long id);

    /** 阅读数原子自增：UPDATE ... SET views = views + 1，避免"读出来加一再写回"的并发丢失 */
    int increaseViews(@Param("id") Long id);
}
