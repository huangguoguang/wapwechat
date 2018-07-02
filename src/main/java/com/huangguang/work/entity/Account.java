package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description: 账户对象
 * User : huangguang
 * DATE : 2018-06-28 14:07
 */
@Data
public class Account {
       @JSONField(name = "Uin")
       private Long uin;

       @JSONField(name = "UserName")
       private String userName;

       @JSONField(name = "NickName")
       private String nickName;

       @JSONField(name = "HeadImgUrl")
       private String headImgUrl;

       @JSONField(name = "ContactFlag")
       private Integer contactFlag;

       @JSONField(name = "MemberCount")
       private Integer merberCount;

       @JSONField(name = "MemberList")
       private List<Member> members;

       @JSONField(name = "RemarkName")
       private String remarkName;

       @JSONField(name = "HideInputBarFlag")
       private String hideInputBarFlag;

       @JSONField(name = "Sex")
       private Integer sex;

       @JSONField(name = "Signature")//签名
       private String signature;

       @JSONField(name = "VerifyFlag")
       private Integer verifyFlag;  //区别好友类型

       @JSONField(name = "OwnerUin")
       private Integer ownerUin;

       @JSONField(name = "PYInitial")
       private String pyInitial;

       @JSONField(name = "PYQuanPin")
       private String pyQuanPin;

       @JSONField(name = "RemarkPYInitial")
       private String remarkPYInitial;

       @JSONField(name = "RemarkPYQuanPin")
       private String remarkPYQuanPin;

       @JSONField(name = "StarFriend")
       private Integer starFriend;

       @JSONField(name = "AppAccountFlag")
       private Integer appAccountFlag;

       @JSONField(name = "Statues")
       private Integer statues;

       @JSONField(name = "AttrStatus")
       private Integer attrStatus;

       @JSONField(name = "Province")
       private String province;

       @JSONField(name = "City")
       private String city;

       @JSONField(name = "Alias")
       private String alias;

       @JSONField(name = "SnsFlag")
       private String snsFlag;

       @JSONField(name = "UniFriend")
       private String uniFriend;

       @JSONField(name = "DisplayName")
       private String displayName;

       @JSONField(name = "ChatRoomId")
       private String chatRoomId;

       @JSONField(name = "KeyWord")
       private String keyWord;

       @JSONField(name = "EncryChatRoomId")
       private String encryChatRoomId;//群组ID

       @JSONField(name = "IsOwner")
       private Integer isOwner;
}
