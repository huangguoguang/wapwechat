package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * APP信息
 *
 */
@Data
public class AppInfo {

    @JSONField(name = "AppID")
    private String appId;

    @JSONField(name = "Type")
    private Integer type;

}
