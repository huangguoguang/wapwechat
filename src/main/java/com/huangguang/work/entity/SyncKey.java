package com.huangguang.work.entity;

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
    private String count;
    private List<KeyItem> list;
}
