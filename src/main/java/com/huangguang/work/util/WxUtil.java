package com.huangguang.work.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huangguang.work.entity.SyncKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
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

    public static String getInitJson(Map<String, Object> initMap) {
        String initJson = "{\"BaseRequest\":{\"Uin\":\"[wxuin]\",\"Sid\":\"[wxsid]\",\"Skey\":\"[skey]\",\"DeviceID\":\"[deviceid]\"}}";
        String deviceId = "e" + getNormalRandom(15);//15位随机数字
        String wxUin = initMap.get("wxuin").toString();
        String sid = initMap.get("wxsid").toString();
        String skey = initMap.get("skey").toString();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Uin", wxUin);
        jsonObject.put("Sid", sid);
        jsonObject.put("Skey", skey);
        jsonObject.put("DeviceID", deviceId);
        initJson = initJson.replace("[wxuin]", wxUin).replace("[wxsid]", sid).replace("[skey]", skey).replace("[deviceid]", deviceId);
        log.info("init json : " + initJson);
        return initJson;
    }

    public static String getSyncJson(String sid, String uin, String skey, String synckey, String passTicket) {
        String syncJson = "{\"BaseRequest\":{\"Uin\":\"[wxuin]\",\"Sid\":\"[wxsid]\",\"Skey\":\"[skey]\",\"DeviceID\":\"[deviceid]\"},\"SyncKey\":\"[SyncKey]\",\"rr\":\"[rr]\"}";
        String deviceId = "e" + getNormalRandom(15);//15位随机数字
        syncJson = syncJson.replace("[wxuin]", uin).replace("[wxsid]", sid).replace("[skey]", skey)
                .replace("[deviceid]", deviceId).replace("[SyncKey]", synckey).replace("[rr]", getRandomNum(10));
        String syncUrl = UrlConstants.syncUrl.replace("SID", sid).replace("SKEY", skey).replace("PASSTICKET", passTicket);
        String result = HttpClientUtil.jsonPost(syncUrl, syncJson, "UTF-8");
        log.info("获取消息返回: {}", result);
        return result;
    }

    public static void main(String[] args) {
        String saa = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage?ticke";
        System.out.println(getPushDomainName(saa));
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
        JSONArray array = JSONObject.parseArray(syncKeyObject.getList());
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
        return sb.toString();
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
     * @param sid
     * @param uin
     * @param skey
     * @param synckey
     * @param pushDomainName
     * @return
     */
    public static String doSyncCheck(String sid, String uin, String skey, String synckey, String pushDomainName) throws UnsupportedEncodingException {
        String deviceId = "e" +WxUtil.getNormalRandom(15);
        String checkUrl = UrlConstants.syncChekUrl.replace("PHSHDOMAINNAME", pushDomainName).replace("TIMESTAMP1", String.valueOf(System.currentTimeMillis()))
                .replace("SKEY", skey).replace("SID", sid).replace("UIN", uin).replace("DEVICEID", deviceId)
                .replace("SYNCKEY", synckey).replace("TIMESTAMP", String.valueOf(System.currentTimeMillis()));
        checkUrl = checkUrl.replace("@", "%40").replace("|", "%7C");
        String result = HttpClientUtil.httpGet(checkUrl, new HashMap<>(), "UTF-8");
        log.info("心跳返回: {}", result);
        //window.synccheck={retcode:"1100",selector:"0"}
        return result;
    }
}
