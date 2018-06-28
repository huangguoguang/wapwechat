package com.huangguang.work.entity;

import lombok.Data;

/**
 * 群成员
 *
 * @author biezhi
 * @date 2018/1/19
 */
@Data
public class Member {

    private Long uin;

    private String userName;

    private String nickName;

    private String displayName;

    private String remarkName;

    private Long attrStatus;

    private Integer memberStatus;

    private String keyWord;

}
