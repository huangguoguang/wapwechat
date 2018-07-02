package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * WebSync 响应
 *
 */
@Data
public class WebSyncResponse  extends JsonResponse{

    @JSONField(name = "AddMsgCount")
    private Integer addMsgCount;

    @JSONField(name = "AddMsgList")
    private List<Message> addMessageList;

    @JSONField(name = "ModContactCount")
    private Integer modContactCount;

    @JSONField(name = "ModContactList")
    private List<Account> modContactList;

    @JSONField(name = "DelContactCount")
    private Integer delContactCount;

    @JSONField(name = "DelContactList")
    private List<Account> delContactList;

    @JSONField(name = "ModChatRoomMemberCount")
    private Integer modChatRoomMemberCount;

    @JSONField(name = "ModChatRoomMemberList")
    private List<Account> modChatRoomMemberList;

    @JSONField(name = "Profile")
    private Profile profile;

    @JSONField(name = "ContinueFlag")
    private Integer continueFlag;

    @JSONField(name = "SyncKey")
    private SyncKey syncKey;

    @JSONField(name = "SKey")
    private String sKey;

    @JSONField(name = "SyncCheckKey")
    private SyncKey syncCheckKey;

}
