package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.huangguang.work.enums.MsgType;
import lombok.Data;

/**
 * 微信原始消息体
 *
 */
@Data
public class Message {

    @JSONField(name = "MsgId")
    private String id;

    @JSONField(name = "FromUserName")
    private String fromUserName;

    @JSONField(name = "ToUserName")
    private String toUserName;

    @JSONField(name = "MsgType")
    private Integer type;

    @JSONField(name = "Content")
    private String content;

    @JSONField(name = "Status")
    private Integer status;

    @JSONField(name = "ImgStatus")
    private Integer imgStatus;

    @JSONField(name = "CreateTime")
    private Long createTime;

    @JSONField(name = "VoiceLength")
    private Long voiceLength;

    @JSONField(name = "PlayLength")
    private Long playLength;

    @JSONField(name = "FileName")
    private String fileName;

    @JSONField(name = "FileSize")
    private String fileSize;

    @JSONField(name = "MediaId")
    private String mediaId;

    @JSONField(name = "Url")
    private String url;

    @JSONField(name = "AppMsgType")
    private Integer appMsgType;

    @JSONField(name = "StatusNotifyCode")
    private Integer statusNotifyCode;

    @JSONField(name = "StatusNotifyUserName")
    private String statusNotifyUserName;

    @JSONField(name = "RecommendInfo")
    private Recommend recommend;

    @JSONField(name = "ForwardFlag")
    private Integer forwardFlag;

    @JSONField(name = "AppInfo")
    private AppInfo appInfo;

    @JSONField(name = "HasProductId")
    private Integer hasProductId;

    @JSONField(name = "Ticket")
    private String ticket;

    @JSONField(name = "ImgHeight")
    private Integer imgHeight;

    @JSONField(name = "ImgWidth")
    private Integer imgWidth;

    @JSONField(name = "SubMsgType")
    private Integer subMsgType;

    @JSONField(name = "NewMsgId")
    private Long newMsgId;

    @JSONField(name = "OriContent")
    private String oriContent;

    @JSONField(name = "EncryFileName")
    private String encryFileName;

    /**
     * 是否是群聊消息
     *
     * @return 返回是否是群组消息
     */
    public boolean isGroup() {
        return fromUserName.contains("@@") || toUserName.contains("@@");
    }

    public MsgType msgType() {
        switch (this.type) {
            case 1:
                return MsgType.TEXT;
            case 3:
                return MsgType.IMAGE;
            case 34:
                return MsgType.VOICE;
            case 37:
                return MsgType.ADD_FRIEND;
            case 42:
                return MsgType.PERSON_CARD;
            case 43:
                return MsgType.VIDEO;
            case 47:
                return MsgType.EMOTICONS;
            case 49:
                return MsgType.SHARE;
            case 51:
                return MsgType.CONTACT_INIT;
            case 62:
                return MsgType.VIDEO;
            case 10000:
                return MsgType.SYSTEM;
            case 10002:
                return MsgType.REVOKE_MSG;
            default:
                return MsgType.UNKNOWN;
        }
    }
}
