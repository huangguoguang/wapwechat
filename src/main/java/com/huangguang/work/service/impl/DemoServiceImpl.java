package com.huangguang.work.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huangguang.work.entity.SyncKey;
import com.huangguang.work.entity.User;
import com.huangguang.work.service.DemoService;
import com.huangguang.work.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HTTP;
import org.jdom2.JDOMException;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.xml.soap.Node;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String qcrodeUrl = "https://login.weixin.qq.com/qrcode/" + uuid;
        log.info("登录二维码链接{}", qcrodeUrl);
        return qcrodeUrl;
    }

    @Override
    public Map<String, Object> loginListen(String uuid) {
        Map<String, Object> map = new HashMap<>();
        try {
            String loginUrl = "https://login.wx.qq.com/cgi-bin/mmwebwx-bin/login?loginicon=true&uuid=UUID&tip=0&r=-643281714&_=TIMESTAMP";
            String replaceUrl = loginUrl.replace("UUID", uuid).replace("TIMESTAMP", String.valueOf(System.currentTimeMillis()));
            log.info("检测二维码扫描状态：{}", replaceUrl);
            String result = HttpClientUtil.httpGet(replaceUrl, new HashMap<>());
             log.info("检测二维码扫描状态结果为{}", result);
            //window.code=408;
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
                String checkLoginResult = HttpClientUtil.httpGet(newLoginUrl, new HashMap<>(), "UTF-8");
                log.info("检查登录结果为{}", checkLoginResult);
                Map<String, Object> initMap = NodeUtil.doXMLParse(checkLoginResult);
                if (initMap.get("ret").equals("0")) {
                    String passTiket = initMap.get("pass_ticket").toString();
                    String initUrl = UrlConstants.initUrl.replace("PASSTICKET", passTiket);
                    log.info("initURL " + initUrl);
                    String initResult = HttpClientUtil.jsonPost(initUrl, WxUtil.getInitJson(initMap), "UTF-8");
                    log.info("initResult  " + initResult);
                    //加载用户信息
                    JSONObject rootJson = JSONObject.parseObject(initResult);
                    User user = JSONObject.parseObject(rootJson.get("User").toString(), User.class);
                    System.out.println(user.toString());
                    String syncKeyParam = rootJson.get("SyncKey").toString();
                    map.put("success", true);
                    map.put("skey", initMap.get("skey"));
                    map.put("sid", initMap.get("wxsid"));
                    map.put("uin", initMap.get("wxuin"));
                    map.put("synckey", syncKeyParam);
                    map.put("passTicket", passTiket);
                    map.put("user", user);
                } else {
                    log.error("登录失败");
                    map.put("success", false);
                }
                map.put("pushDomainName", pushDomainName);
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
    public Map<String, Object> syncCheck(String sid, String uin, String skey, String synckey, String pushDomainName, String passTicket) {
        try {
            String syncKeyParam = WxUtil.convertSyncKey(synckey);
            String result = WxUtil.doSyncCheck(sid, uin, skey, syncKeyParam, pushDomainName);
            JSONObject jsonObject = JSONObject.parseObject(result.replace("window.synccheck=", ""));
            String retcode = jsonObject.get("retcode").toString();
            int selector = Integer.valueOf(jsonObject.get("selector").toString());
            String error = "";
            int value = 0;

            if ("1100".equals(retcode) || "1101".equals(retcode)) {
                //条件成立,循环10次
                for (int i = 0; i < 10; i++) {
                    result = WxUtil.doSyncCheck(sid, uin, skey, synckey, pushDomainName);
                    jsonObject = JSONObject.parseObject(result.replace("window.synccheck=", ""));
                    retcode = jsonObject.get("retcode").toString();
                    selector = Integer.valueOf(jsonObject.get("selector").toString());
                    if (!"1100".equals(retcode) && !"1101".equals(retcode)) {
                        //如果retcode != 1100 且 != 1101 跳出循环
                        break;
                    }
                }
                if ("1100".equals(retcode) || "1101".equals(retcode)) {
                    error = "1101";
                    value = 1;
                }
            }
            if (selector == 0 ) {
                log.info("没有数据");
                value = 0;
            }
            if (selector > 0) {
                log.info("有数据到达");
                value = 1;
            }
            if (value == 1) {
                boolean sync = doSync(sid, uin, skey, synckey, pushDomainName, passTicket, error);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private boolean doSync(String sid, String uin, String skey, String synckey, String pushDomainName, String passTicket, String error) {
        if ("1101".equals(error)) {
            return false;
        }
        String result = WxUtil.getSyncJson(sid, uin, skey, synckey, passTicket);
        JSONObject jsonObject = JSONObject.parseObject(result);
        System.out.println("消息记录" + jsonObject.get("AddMsgCount"));
        return true;
    }


    public static void main(String[] args) throws Exception {
    }

}
