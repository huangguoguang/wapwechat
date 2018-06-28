package com.huangguang.work.entity;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:会话对象
 * <p>
 * User : huangguang
 * DATE : 2018-06-28 14:29
 */
@Data
public class LoginSession {
    private User user;

    private Account account;

    private String url;

    private String fileUrl;

    private String syncUrl;

    private String deviceId;

    private String sKey;

    private String wxSid;

    private String wxUin;

    private String passTicket;

    private String syncKeyStr;

    private Integer inviteStartCount;

    private BaseRequest baseRequest;

    private SyncKey syncKey;

    public String getSyncOrUrl() {
        if (StringUtils.isNotEmpty(this.syncUrl)) {
            return this.syncUrl;
        }
        return this.url;
    }

    public String getFileUrl() {
        if (StringUtils.isNotEmpty(this.fileUrl)) {
            return this.fileUrl;
        }
        return this.url;
    }

    public void setSyncKey(SyncKey syncKey) {
        this.syncKey = syncKey;

        StringBuilder syncKeyBuf = new StringBuilder();
        for (KeyItem item : syncKey.getList()) {
            syncKeyBuf.append("|").append(item.getKey()).append("_").append(item.getVal());
        }
        if (syncKeyBuf.length() > 0) {
            this.syncKeyStr = syncKeyBuf.substring(1);
        }
    }
}
