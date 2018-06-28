package com.huangguang.work.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huangguang.work.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-25 15:55
 */
@Slf4j
public class WxUtil {
    public static String cookies = "";

    public static String uuidUrl = "https://login.wx.qq.com/jslogin?appid=wx782c26e4c19acffb&redirect_uri=https%3A%2F%2Fwx.qq.com%2Fcgi-bin%2Fmmwebwx-bin%2Fwebwxnewloginpage&fun=new&lang=zh_CN&_=TIMESTAMP";

    static {
        try {
            cookies = "pgv_pvid=2349344264; pgv_pvi=371976192; pt2gguin=o0330197411; RK=TEqMmRLFeE; ptcz=0c861dd53b748a0d4c425b7958d354315fc864c7376ec9bb9e009d99caa89281; o_cookie=330197411; pac_uid=1_330197411; tvfe_boss_uuid=282e7884c3287810; wxuin=2418455500; webwxuvid=68252693b66488fb6268786e5aea08c0579e848f8406289a2c843d0655d03e850fdcd91ffd63c1f99c4b1531f6ddf65b; last_wxuin=2418455500; pgv_info=ssid=s7006621335; mm_lang=zh_CN; MM_WX_NOTIFY_STATE=1; MM_WX_SOUND_STATE=1; refreshTimes=5; webwx_auth_ticket=CIsBEO70p/MHGoAB9sZr3HFBxUzG7k3RA/8V6gtO/LdHlU/wpKGjlbSuI2o/iOHSEn5lkdfbi0JA4vCnaWPT4no8xFAgbJgtdDHd9Zp+TXavaOvpxelTjjdkPfN5tPFCp8DwFgftk6A/42MDX8V4QIL9jo+OxiO0+85mZmQyxUxm2WCv0bGaVmmh5AI=; login_frequency=1; wxloadtime=1530086030_expired; wxpluginkey=1530085918; pgv_si=s8927884288";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 随机生成num位随机数，前面有-号
     * @param num
     * @return
     */
    public static String getRandomNum(int num) {
        StringBuilder sb = new StringBuilder();
        sb.append("-");
        Random random = new Random();
        for (int i= 0; i < num; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String getNormalRandom(int num) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i= 0; i < num; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 取全局cookies
     * @return
     * @throws Exception
     */
    public static String getCookies() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        Map<String, String> querys = new HashMap<>();
        HttpResponse httpResponse = HttpUtils.doGet("https://wx.qq.com", "", "", headers, querys);
        Header[] heades = httpResponse.getAllHeaders();
        for (int i =0;i< heades.length; i++) {
            if (heades[i].getName().equals("Set-Cookie")) {
                cookies = heades[i].getValue();
            }
        }
        log.info("cookies: " + cookies);
        return cookies;
    }

    public static String getUUID() throws Exception {
        long timestamp = System.currentTimeMillis();
        String firstUrl = uuidUrl.replace("TIMESTAMP", String.valueOf(timestamp));
        log.info("请求uuid地址:{}", firstUrl);
        Map<String, String> headers = new HashMap<String, String>();
        Map<String, String> querys = new HashMap<>();
        headers.put("Cookie", cookies);
        HttpResponse httpResponse = HttpUtils.doGet(uuidUrl, "", "", headers, querys);
        String result = EntityUtils.toString(httpResponse.getEntity());
        log.info("uuid请求结果:{}", result);
        if (result.indexOf("window.QRLogin.code = 200") != -1) {
            return result.replace("window.QRLogin.code = 200; window.QRLogin.uuid = \"", "").replace("\";", "");
        }
        return "";
    }

    public static String getInitJson(LoginSession loginSession) {
        String initJson = "{\"BaseRequest\":{\"Uin\":\"[wxuin]\",\"Sid\":\"[wxsid]\",\"Skey\":\"[skey]\",\"DeviceID\":\"[deviceid]\"}}";
        String deviceId = "e" + getNormalRandom(15);//15位随机数字
        String wxUin = loginSession.getWxUin();
        String sid = loginSession.getWxSid();
        String skey = loginSession.getSKey();
        initJson = initJson.replace("[wxuin]", wxUin).replace("[wxsid]", sid).replace("[skey]", skey).replace("[deviceid]", deviceId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("BaseRequest", loginSession.getBaseRequest());
        System.out.println(initJson);
        log.info("init json : " + jsonObject.toString());
        return jsonObject.toString();
    }

    public static String getSyncJson(LoginSession loginSession) {
        String syncJson = "{\"BaseRequest\":{\"Uin\":\"[wxuin]\",\"Sid\":\"[wxsid]\",\"Skey\":\"[skey]\",\"DeviceID\":\"[deviceid]\"},\"SyncKey\":\"[SyncKey]\",\"rr\":\"[rr]\"}";
        String deviceId = "e" + getNormalRandom(15);//15位随机数字
        syncJson = syncJson.replace("[wxuin]", loginSession.getWxUin()).replace("[wxsid]", loginSession.getWxSid()).replace("[skey]", loginSession.getSKey())
                .replace("[deviceid]", deviceId).replace("[SyncKey]", loginSession.getSyncKeyStr()).replace("[rr]", getRandomNum(10));
        String syncUrl = UrlConstants.syncUrl.replace("SID", loginSession.getWxSid()).replace("SKEY", loginSession.getSKey()).replace("PASSTICKET", loginSession.getPassTicket());
        String result = HttpClientUtil.jsonPost(syncUrl, syncJson, "UTF-8");
        log.info("获取消息返回: {}", result);
        return result;
    }

    public static void main(String[] args) {
        String a = "https://login.weixin.qq.com/qrcode/%s";
        System.out.println(String.format(a, "aaaaa"));
    }


    /**
     * 解析syncKey参数为形如
     * synckey=1_667285982|2_667286229|3_667285960|11_667286030|201_1530003357|203_1529980319
     * 以下格式
     * @param syncKey
     * @return
     */
    public static String convertSyncKey(String syncKey) {
        SyncKey syncKeyObject = JSONObject.parseObject(syncKey, SyncKey.class);
        int count = Integer.parseInt(syncKeyObject.getCount());
/*        JSONArray array = JSONObject.parseArray(syncKeyObject.getList());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            JSONObject jsonObject = JSONObject.parseObject(array.get(i).toString());
            String key = jsonObject.get("Key").toString();
            String value = jsonObject.get("Val").toString();
            sb.append(key).append("_").append(value);
            if (i < count -1) {
                sb.append("|");
            }
        }
        return sb.toString();*/
return "";
    }


    /**
     * 获取syncCheck域名
     * @param newLoginUrl
     * @return
     */
    public static String getPushDomainName(String newLoginUrl) {
        newLoginUrl = newLoginUrl.replace("https://", "");
        String domainName = newLoginUrl.substring(0, newLoginUrl.indexOf("/"));
        log.info("domainName: {}", domainName);
        String pushDomainName = "";
        if (domainName.equals(UrlConstants.domainName)) {
            pushDomainName = UrlConstants.pushDomainName;
        } else {
            pushDomainName = UrlConstants.pushDomainName2;
        }
        log.info("pushDomainName: {}", pushDomainName);
        return pushDomainName;
    }


    /**
     * 执行syncCheck
     * @param loginSession
     * @return
     */
    public static SyncCheckRet doSyncCheck(LoginSession loginSession) {
        String deviceId = "e" +WxUtil.getNormalRandom(15);
        String checkUrl = String.format(UrlConstants.syncChekUrl, loginSession.getSyncOrUrl(), System.currentTimeMillis(),
                loginSession.getSKey(), loginSession.getWxSid(), loginSession.getWxUin(), deviceId, loginSession.getSyncKeyStr(), System.currentTimeMillis());
        checkUrl = checkUrl.replace("@", "%40").replace("|", "%7C");
        log.info("心跳地址:{}", checkUrl);
        String result = HttpClientUtil.httpGet(checkUrl, new HashMap<>(), "UTF-8");

        SyncCheckRet syncCheckRet = JSONObject.parseObject(result.replace("window.synccheck=", ""), SyncCheckRet.class);
        log.info("心跳返回: {}", result);
        //window.synccheck={retcode:"1100",selector:"0"}
        return syncCheckRet;
    }

    public static String wxStatusNotify(Map<String, Object> initMap, String userName) {
        String deviceId = "e" +WxUtil.getNormalRandom(15);
        String json = "{\"BaseRequest\":{\"Uin\":\"UIN\",\"Sid\":\"SID\",\"Skey\":\"SKEY\",\"DeviceID\":\"DEVICEID\"},\"Code\":3,\"FromUserName\":\"NAME\",\"ToUserName\":\"NAME\",\"ClientMsgId\":\"TIME\"}";
        json = json.replace("UIN", initMap.get("wxuin").toString()).replace("SID", initMap.get("wxsid").toString()).replace("SKEY", initMap.get("skey").toString())
                .replace("DEVICEID", deviceId).replace("NAME", userName).replace("TIME", String.valueOf(System.currentTimeMillis()));
        String notifyUrl = UrlConstants.notifyUrl.replace("PASSTICKET", initMap.get("pass_ticket").toString());
        String result = HttpClientUtil.jsonPost(notifyUrl, json, "UTF-8");
        System.out.println(result);
        return result;
    }

    /**
     * 处理登录成功结果，写入loginSession对象
     * @param checkLoginResult
     * @param loginSession
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    public static LoginSession doLoginSession(String checkLoginResult, LoginSession loginSession) throws JDOMException, IOException {
        Map<String, Object> initMap = NodeUtil.doXMLParse(checkLoginResult);
        if (initMap.get("ret").equals("0")) {
            log.info("登录成功");
            loginSession.setPassTicket(initMap.get("pass_ticket").toString());
            loginSession.setWxSid(initMap.get("wxsid").toString());
            loginSession.setWxUin(initMap.get("wxuin").toString());
            loginSession.setSKey(initMap.get("skey").toString());
            //初始化用户信息
            loginSession = init(loginSession);
            String notifyResult = WxUtil.wxStatusNotify(initMap, loginSession.getUser().getUserName());
            System.out.println(notifyResult);
            return loginSession;
        }
        log.error("登录失败");
        return loginSession;
    }

    private static LoginSession init(LoginSession loginSession) {
        String initUrl = String.format(UrlConstants.initUrl, loginSession.getPassTicket());
        log.info("initURL " + initUrl);
        BaseRequest baseRequest = WxUtil.getBaseRequest(loginSession);
        loginSession.setBaseRequest(baseRequest);
        String initResult = HttpClientUtil.jsonPost(initUrl, WxUtil.getInitJson(loginSession), "UTF-8");
        log.info("initResult  " + initResult);
        //加载用户信息
        JSONObject rootJson = JSONObject.parseObject(initResult);
        User user = JSONObject.parseObject(rootJson.get("User").toString(), User.class);
        loginSession.setUser(user);
        System.out.println(user.toString());
        String syncKeyParam = rootJson.get("SyncKey").toString();
        SyncKey syncKey = JSONObject.parseObject(rootJson.get("SyncKey").toString(), SyncKey.class);
        loginSession.setSyncKeyStr(syncKeyParam);
        loginSession.setSyncKey(syncKey);
        return loginSession;
    }

    private static BaseRequest getBaseRequest(LoginSession loginSession) {
        String deviceId = "e" + getNormalRandom(15);//15位随机数字
        BaseRequest baseRequest = new BaseRequest();
        baseRequest.setUin(loginSession.getWxUin());
        baseRequest.setSid(loginSession.getWxSid());
        baseRequest.setSkey(loginSession.getSKey());
        baseRequest.setDeviceID(deviceId);
        return baseRequest;
    }
}
