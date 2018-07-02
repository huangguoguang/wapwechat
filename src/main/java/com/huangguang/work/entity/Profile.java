package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Profile 信息
 *
 * @author biezhi
 * @date 2018/1/20
 */
@Data
public class Profile {

    @JSONField(name =  "BitFlag")
    private Integer bitFlag;

    @JSONField(name = "UserName")
    private UserName userName;

    @JSONField(name = "NickName")
    private UserName nickName;

    @JSONField(name = "BindUin")
    private Integer bindUin;

    @JSONField(name = "BindEmail")
    private UserName bindEmail;

    @JSONField(name = "BindMobile")
    private UserName bindMobile;

    @JSONField(name = "Status")
    private Integer status;

    @JSONField(name = "Sex")
    private Integer sex;

    @JSONField(name = "PersonalCard")
    private Integer personalCard;

    @JSONField(name = "Alias")
    private String alias;

    @JSONField(name = "HeadImgUpdateFlag")
    private Integer headImgUpdateFlag;

    @JSONField(name = "HeadImgUrl")
    private String headImgUrl;

    @JSONField(name = "Signature")
    private String signature;

    @Data
    static class UserName {
        @JSONField(name = "Buff")
        String buff;
    }

}
