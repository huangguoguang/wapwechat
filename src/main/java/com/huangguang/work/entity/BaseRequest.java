package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-28 11:35
 */
@Data
public class BaseRequest {
    @JSONField(name = "Skey")
    private String skey;

    @JSONField(name = "Sid")
    private String sid;

    @JSONField(name = "Uin")
    private String uin;

    @JSONField(name = "DeviceID")
    private String deviceID;
}
