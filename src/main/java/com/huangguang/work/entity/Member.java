package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 群成员
 *
 * @author biezhi
 * @date 2018/1/19
 */
@Data
public class Member {

    @JSONField(name = "Uin")
    private Long uin;

    @JSONField(name = "UserName")
    private String userName;

    @JSONField(name = "NickName")
    private String nickName;

    @JSONField(name = "AttrStatus")
    private Long attrStatus;

    @JSONField(name = "PYInitial")
    private String pyInitial;

    @JSONField(name = "PYQuanPin")
    private String pyQuanPin;

    @JSONField(name = "RemarkPYInitial")
    private String remarkPyInitial;

    @JSONField(name = "RemarkPYQuanPin")
    private String remarkPyQuanPin;

    @JSONField(name = "MemberStatus")
    private Integer memberStatus;

    @JSONField(name = "DisplayName")
    private String displayName;

    @JSONField(name = "KeyWord")
    private String keyWord;

}
