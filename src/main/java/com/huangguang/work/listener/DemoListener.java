package com.huangguang.work.listener;

import com.huangguang.work.event.DemoEvent;
import com.huangguang.work.service.DemoService;
import com.huangguang.work.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-22 14:46
 */
@Slf4j
@Service
public class DemoListener {
    @Autowired
    private DemoService demoService;

    @Async
    @EventListener
    public void loginListen(ApplicationEvent event) {
        if (event instanceof DemoEvent) {
            log.info("监听到事件：");
            DemoEvent demoEvent = (DemoEvent) event;
            String uuid = (String) demoEvent.getSource();
            for (int i = 0; ; i++) {
                int check = checkLogin(uuid);
                if (check == 3) {
                    log.info("已在手机端确认登录");

                } else if (check == 2) {

                }
            }
        }
    }

    /**
     * 监听登录,用户是否扫码登录
     * @param uuid
     * @return
     */
    private int checkLogin(String uuid) {
/*        String loginUrl = "https://login.wx.qq.com/cgi-bin/mmwebwx-bin/login?loginicon=true&uuid=UUID&tip=0&r=-643281714&_=TIMESTAMP";
        String replaceUrl = loginUrl.replace("UUID", uuid).replace("TIMSTAMP", String.valueOf(System.currentTimeMillis()));
        log.info("监听url：{}", replaceUrl);
        String result = HttpClientUtil.httpGet(replaceUrl, new HashMap<>());
        log.info("监听到的登录结果为{}", result);
        //window.code=408;
        if (result.indexOf("408") != -1) {//未扫码
            return 1;
        } else if (result.indexOf("400") != -1) {//二维码已过期
            return 2;
        } else if (result.indexOf("200") != -1) {
            return 3;
        }*/
        return 0;
    }
}
