package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:订阅号消息
 * User : huangguang
 * DATE : 2018-06-29 15:16
 */
@Data
public class MpSubcribeMsg {

    @JSONField(name = "UserName")
    private String userName;

    @JSONField(name = "MPArticleCount")
    private Integer mpArticleCount;//文章数量

    @JSONField(name = "MPArticleList")
    private List<MpArticle> mpArticleList;//文章列表

    @JSONField(name = "Time")
    private Long time;

    @JSONField(name = "NickName")
    private String nickName;//订阅号名称

}
