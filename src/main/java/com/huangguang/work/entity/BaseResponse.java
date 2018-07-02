package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-29 10:21
 */
@Data
public class BaseResponse {
    @JSONField(name = "Ret")
    private Integer ret;

    @JSONField(name = "ErrMsg")
    private String errMsg;

}
