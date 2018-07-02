package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-27 10:00
 */
@Data
public class SyncKey {
    @JSONField(name = "Count")
    private String count;
    @JSONField(name = "List")
    private List<KeyItem> list;
}
