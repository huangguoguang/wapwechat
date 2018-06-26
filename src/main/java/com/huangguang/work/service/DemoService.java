package com.huangguang.work.service;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-22 15:25
 */
public interface DemoService {

    /**
     * 获取登录二维码所需要的参数
     * @return
     */
    String getUUID();

    String getQcrodeUrl(String uuid);

    Map<String, Object> loginListen(String uuid);
}
