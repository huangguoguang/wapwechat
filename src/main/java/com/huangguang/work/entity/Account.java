package com.huangguang.work.entity;

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
       private Long uin;
       private String userName;
       private String nickName;
       private String headImgUrl;
       private Integer contactFlag;
       private Integer merberCount;
       private List<Member> members;
       private String remarkName;
       private String hideInputBarFlag;
       private Integer sex;
       private String signature;
       private Integer verifyFlag;  //区别好友类型
       private Integer ownerUin;
       private String pyInitial;
       private String pyQuanPin;
       private String remarkPYInitial;
       private String remarkPYQuanPin;
       private Integer starFriend;
       private Integer appAccountFlag;
       private Integer statues;
       private String encryChatRoomId;//群组ID
}
