package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description: 订阅号文章
 * User : huangguang
 * DATE : 2018-06-29 15:19
 */
@Data
public class MpArticle {
    @JSONField(name = "Title")
    private String title;

    @JSONField(name = "Digest")
    private String digest;

    @JSONField(name = "Cover")
    private String cover;

    @JSONField(name = "Url")
    private String url;

}
