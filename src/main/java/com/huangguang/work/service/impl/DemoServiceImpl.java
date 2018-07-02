package com.huangguang.work.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.huangguang.work.entity.LoginSession;
import com.huangguang.work.entity.SyncCheckRet;
import com.huangguang.work.entity.WeChatMessage;
import com.huangguang.work.entity.WebSyncResponse;
import com.huangguang.work.enums.RetCode;
import com.huangguang.work.service.DemoService;
import com.huangguang.work.util.HttpClientUtil;
import com.huangguang.work.util.UrlConstants;
import com.huangguang.work.util.WxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
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
                LoginSession loginSession = WxUtil.doLoginSession(result);
                map.put("loginSession", loginSession);
                return map;
            } else if (result.indexOf("201") != -1) {
                //已扫码，未登录
                map.put("code", 4);
                int start = result.indexOf("window.userAvatar = '");
                result = result.substring(start).replace("window.userAvatar = '", "").replace("';", "");
                log.info("用户图像{}", result);
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
            SyncCheckRet syncCheckRet = WxUtil.doSyncCheck(loginSession);
            RetCode retCode = RetCode.parse(syncCheckRet.getRetCode());
            if (retCode == RetCode.UNKNOWN) {
                log.info("未知状态");
                loginSession.setSuccess(false);
            } else if (retCode == RetCode.LOGIN_OTHERWISE) {
                log.info(RetCode.LOGIN_OTHERWISE.getType());
                loginSession.setSuccess(false);
            } else if (retCode == RetCode.MOBILE_LOGIN_OUT) {
                log.info(RetCode.MOBILE_LOGIN_OUT.getType());
                loginSession.setSuccess(false);
            } else if (retCode == RetCode.NORMAL){
                log.info("登录状态正常");
                if (syncCheckRet.getSelector() == 0) {
                    log.info("没有数据");
                }
                if (syncCheckRet.getSelector() > 0) {
                    log.info("有数据到达");
                    Map<String, Object> resultMap = doSync(loginSession);
                    loginSession = (LoginSession) resultMap.get("loginSession");
                    WebSyncResponse sync = (WebSyncResponse) resultMap.get("webSyncResponse");
                    List<WeChatMessage> weChatMessageList = WxUtil.processMsg(loginSession, sync);
                    respMap.put("loginSession", loginSession);
                    respMap.put("messageList", weChatMessageList);
                    return respMap;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loginSession.setSuccess(false);
        }
        respMap.put("loginSession", loginSession);
        return respMap;
    }

    private Map<String, Object> doSync(LoginSession loginSession) {
        String result = WxUtil.getSyncJson(loginSession);
        Map<String, Object> respMap = new HashMap<>();
        WebSyncResponse webSyncResponse = JSONObject.parseObject(result, WebSyncResponse.class);
        if (!webSyncResponse.success()) {
            log.error("获取消息失败");
            respMap.put("loginSession", loginSession);
            respMap.put("webSyncResponse", webSyncResponse);
            return respMap;
        }
        log.info("消息记录" + webSyncResponse.getAddMsgCount());
        loginSession.setSyncKey(webSyncResponse.getSyncKey());//更新syncKey
        respMap.put("loginSession", loginSession);
        respMap.put("webSyncResponse", webSyncResponse);
        return respMap;
    }

}
