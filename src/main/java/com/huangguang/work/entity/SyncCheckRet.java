package com.huangguang.work.entity;

import com.huangguang.work.enums.RetCode;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 心跳检查返回
 */
@Data
public class SyncCheckRet {

    private int retCode;
    private int selector;

}
