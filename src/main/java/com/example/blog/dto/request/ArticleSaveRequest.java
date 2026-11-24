package com.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文章创建与更新共用一个请求体。
 *
 * PUT 的语义是"全量替换"：更新时必须带全部字段，所以必填规则与 POST 一致。
 * 如果业务需要"局部修改"，才拆出 PatchRequest——当前场景不需要。
 */
@Data
public class ArticleSaveRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长 100 字")
    private String title;

    /** 摘要可留空，留空时后端自动截取正文生成 */
    @Size(max = 255, message = "摘要最长 255 字")
    private String summary;

    @NotBlank(message = "正文不能为空")
    @Size(max = 30000, message = "正文最长 30000 字")
    private String content;

    /** @Pattern 把非法值拦在参数校验层，业务层不用再写 if */
    @Pattern(regexp = "^(draft|published)$", message = "状态只能是 draft 或 published")
    private String status = "published";
}
