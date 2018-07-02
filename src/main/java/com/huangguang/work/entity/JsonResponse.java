package com.huangguang.work.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-29 10:33
 */
@Data
public class JsonResponse {

    @JSONField(name = "BaseResponse")
    private BaseResponse baseResponse;

    public boolean success() {
        return null != baseResponse && baseResponse.getRet().equals(0);
    }
}
