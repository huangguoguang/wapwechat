package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description: 初始化信息列表
 * User : huangguang
 * DATE : 2018-06-29 15:06
 */
@Data
public class InitResponse extends JsonResponse{
    @JSONField(name = "Count")
    private Integer count;

    @JSONField(name = "ContactList")
    private List<Account> contactList;

    @JSONField(name = "SyncKey")
    private SyncKey syncKey;

    @JSONField(name = "User")
    private User user;

    @JSONField(name = "ChatSet")
    private String chatSet;

    @JSONField(name = "SKey")
    private String sKey;

    @JSONField(name = "ClientVersion")
    private Long clientVersion;

    @JSONField(name = "SystemTime")
    private Long systemTime;

    @JSONField(name = "GrayScale")
    private Integer grayScale;

    @JSONField(name = "InviteStartCount")
    private Integer inviteStartCount;

    @JSONField(name = "MPSubscribeMsgCount")
    private Integer mpSubscribeMsgCount;

    @JSONField(name = "MPSubscribeMsgList")
    private List<MpSubcribeMsg> mpSubcribeMsgList;

    @JSONField(name = "ClickReportInterval")
    private Long clickReportInterval;
}
