package com.huangguang.work.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.huangguang.work.entity.LoginSession;
import com.huangguang.work.entity.SyncCheckRet;
import com.huangguang.work.entity.User;
import com.huangguang.work.enums.RetCode;
import com.huangguang.work.service.DemoService;
import com.huangguang.work.util.HttpClientUtil;
import com.huangguang.work.util.NodeUtil;
import com.huangguang.work.util.UrlConstants;
import com.huangguang.work.util.WxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-22 15:26
 */
@Slf4j
@Service
public class DemoServiceImpl implements DemoService {

    @Override
    public String getUUID() {
        try {
            return WxUtil.getUUID();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getQcrodeUrl(String uuid) {
        String qcrodeUrl = String.format(UrlConstants.qcrodeUrl, uuid);
        log.info("登录二维码链接{}", qcrodeUrl);
        return qcrodeUrl;
    }

    @Override
    public Map<String, Object> loginListen(String uuid) {
        Map<String, Object> map = new HashMap<>();
        try {
            String loginUrl = String.format(UrlConstants.loginUrl, uuid, System.currentTimeMillis());
            log.info("检测二维码扫描状态：{}", loginUrl);
            String result = HttpClientUtil.httpGet(loginUrl, new HashMap<>());
            log.info("检测二维码扫描状态结果为{}", result);
            if (result.indexOf("408") != -1) {//未扫码
                map.put("code", 1);
                return map;
            } else if (result.indexOf("400") != -1) {//二维码已过期
                map.put("code", 2);
                return map;
            } else if (result.indexOf("200") != -1) {
                map.put("code", 3);
                int start = result.indexOf("https");
                String newLoginUrl = result.substring(start).replace("\";", "");
                newLoginUrl += "&fun=new&version=v2";
                log.info("手机确认登录，检查登录地址:{}", newLoginUrl);
                String pushDomainName = WxUtil.getPushDomainName(newLoginUrl);
                LoginSession loginSession = new LoginSession();
                loginSession.setSyncUrl(pushDomainName);
                String checkLoginResult = HttpClientUtil.httpGet(newLoginUrl, new HashMap<>(), "UTF-8");
                log.info("检查登录结果为{}", checkLoginResult);
                loginSession = WxUtil.doLoginSession(checkLoginResult, loginSession);
                map.put("loginSession", loginSession);
                return map;
            } else if (result.indexOf("201") != -1) {
                //已扫码，未登录
                map.put("code", 4);
                int start = result.indexOf("window.userAvatar = '");
                result = result.substring(start).replace("window.userAvatar = '", "").replace("';", "");
                System.out.println(result);
                map.put("img", result);
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("code", 0);
        return map;
    }

    @Override
    public Map<String, Object> syncCheck(LoginSession loginSession) {
        Map<String, Object> respMap = new HashMap<>();
        try {
            String error = "";
            int value = 0;
            SyncCheckRet syncCheckRet = WxUtil.doSyncCheck(loginSession);

            if (syncCheckRet.getRetCode() == RetCode.NORMAL) {
                log.info("未知状态");
            } else if (syncCheckRet.getRetCode() == RetCode.LOGIN_OTHERWISE) {
                log.info(RetCode.LOGIN_OTHERWISE.getType());
            } else if (syncCheckRet.getRetCode() == RetCode.MOBILE_LOGIN_OUT) {
                log.info(RetCode.MOBILE_LOGIN_OUT.getType());
            } else {
                log.info("登录状态正常");
            }
            if (syncCheckRet.getSelector() == 0) {
                log.info("没有数据");
                value = 0;
            }
            if (syncCheckRet.getSelector() > 0) {
                log.info("有数据到达");
                value = 1;
            }
            if (value == 1) {
                boolean sync = doSync(loginSession);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean doSync(LoginSession loginSession) {
        String result = WxUtil.getSyncJson(loginSession);
        JSONObject jsonObject = JSONObject.parseObject(result);
        System.out.println("消息记录" + jsonObject.get("AddMsgCount"));
        return true;
    }

}
