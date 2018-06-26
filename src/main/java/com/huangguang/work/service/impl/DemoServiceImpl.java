package com.huangguang.work.service.impl;

import com.huangguang.work.service.DemoService;
import com.huangguang.work.util.HttpClientUtil;
import com.huangguang.work.util.NodeUtil;
import com.huangguang.work.util.UrlConstants;
import com.huangguang.work.util.WxUtil;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.JDOMException;
import org.springframework.stereotype.Service;

import javax.xml.soap.Node;
import java.io.IOException;
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
        String oneUrl = "https://login.wx.qq.com/jslogin?appid=wx782c26e4c19acffb&redirect_uri=https%3A%2F%2Fwx.qq.com%2Fcgi-bin%2Fmmwebwx-bin%2Fwebwxnewloginpage&fun=new&lang=zh_CN&_=AAAA";
        long timestamp = System.currentTimeMillis();
        String firstUrl = oneUrl.replace("AAAA", String.valueOf(timestamp));
        log.info("第一次请求地址:{}", firstUrl);
        String result = HttpClientUtil.httpGet(firstUrl, new HashMap<>());
        log.info("第一次请求结果:{}", result);
        if (result.indexOf("window.QRLogin.code = 200") != -1) {
            return result.replace("window.QRLogin.code = 200; window.QRLogin.uuid = \"", "").replace("\";", "");
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
                result = result.substring(start).replace("\";", "");
                log.info("已登录，请求新地址:{}", result);
                String redirectHtml = HttpClientUtil.httpGet(result, new HashMap<>());
                log.info(redirectHtml);
                map.put("html", redirectHtml);
                String domainName = redirectHtml.replace("window.code=200;window.redirect_uri=\"", "").replace("\";", "");
                String pushDomainName = "webpush.weixin.qq.com";
                log.info("检测是否登录:{}", domainName);
                String initResult = HttpClientUtil.httpGet(domainName, new HashMap<>());
                log.info("检测结果:{}", initResult);
                Map<String, Object> initMap = NodeUtil.doXMLParse(initResult);
                if ((Integer)initMap.get("ret") == 0) {
                    String passTiket = initMap.get("pass_ticket").toString();
                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put("r", WxUtil.getRandomNum(9));
                    paramMap.put("lang", "zh_CN");
                    paramMap.put("pass_ticket=", passTiket);
                    System.out.println(HttpClientUtil.httpPost(UrlConstants.initUrl, paramMap, "UTF-8", false));
                } else {
                    System.out.println("登录失败");
                }
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

    public static void main(String[] args) throws JDOMException, IOException {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("r", "-1747024806");
        paramMap.put("lang", "zh_CN");
        paramMap.put("pass_ticket", "6Ko1iHw7z5V3nE04R7Q%2B%2FVfhqHzGQdTjMZsh3AV2RVGHnZ2HkhGA651ByNYnlLMP");
        String url = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxinit";
        System.out.println(HttpClientUtil.httpPost(url, paramMap, "UTF-8", false));
    }

}
