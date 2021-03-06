package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-28 11:27
 */
@Data
public class KeyItem {

    @JSONField(name = "Key")
    private String key;

    @JSONField(name = "Val")
    private Integer val;

}
