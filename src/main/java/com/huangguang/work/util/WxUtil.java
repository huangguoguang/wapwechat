package com.huangguang.work.util;

import com.alibaba.fastjson.JSONObject;
import com.huangguang.work.entity.*;
import com.huangguang.work.enums.MsgType;
import com.sun.org.apache.xpath.internal.SourceTree;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.util.EntityUtils;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-25 15:55
 */
@Slf4j
public class WxUtil {

    /**
     * 随机生成num位随机数，前面有-号
     *
     * @param num
     * @return
     */
    public static String getRandomNum(int num) {
        StringBuilder sb = new StringBuilder();
        sb.append("-");
        Random random = new Random();
        for (int i = 0; i < num; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String getNormalRandom(int num) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < num; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String getUUID() throws Exception {
        String firstUrl = UrlConstants.uuidUrl.replace("TIMESTAMP", String.valueOf(System.currentTimeMillis()));
        log.info("请求uuid地址:{}", firstUrl);
        String result = HttpClientUtil.httpGet(firstUrl);
        log.info("uuid请求结果:{}", result);
        if (result.indexOf("window.QRLogin.code = 200") != -1) {
            return result.replace("window.QRLogin.code = 200; window.QRLogin.uuid = \"", "").replace("\";", "");
        }
        return "";
    }

    public static String getInitJson(LoginSession loginSession) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("BaseRequest", loginSession.getBaseRequest());
        log.info("init json : " + jsonObject.toString());
        return jsonObject.toString();
    }

    /**
     * syncCheck请求时的json请求参数
     *
     * @param loginSession
     * @return
     */
    public static String getSyncJson(LoginSession loginSession) {
        String syncUrl = String.format(UrlConstants.syncUrl, loginSession.getWxSid(), loginSession.getSKey(), loginSession.getPassTicket());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("BaseRequest", loginSession.getBaseRequest());
        jsonObject.put("SyncKey", loginSession.getSyncKey());
        jsonObject.put("rr", getRandomNum(10));
        log.info("syncUrl:    " + syncUrl);
        log.info(jsonObject.toJSONString());
        String result = HttpClientUtil.jsonPost(syncUrl, jsonObject.toJSONString(), "UTF-8");
        log.info("获取消息返回: {}", result);
        return result;
    }

    /**
     * 获取syncCheck域名
     *
     * @param newLoginUrl
     * @return
     */
    public static String getSyncCheckDomain(String newLoginUrl) {
        newLoginUrl = newLoginUrl.replace("https://", "");
        String domainName = newLoginUrl.substring(0, newLoginUrl.indexOf("/"));
        log.info("domainName: {}", domainName);
        String checkDomain = "";
        if (domainName.equals(UrlConstants.domainName)) {
            checkDomain = UrlConstants.checkDomain;
        } else {
            checkDomain = UrlConstants.checkDomain2;
        }
        log.info("checkDomain: {}", checkDomain);
        return checkDomain;
    }


    /**
     * 执行syncCheck
     *
     * @param loginSession
     * @return
     */
    public static SyncCheckRet doSyncCheck(LoginSession loginSession) {
        String deviceId = "e" + WxUtil.getNormalRandom(15);
        String checkUrl = String.format(UrlConstants.syncChekUrl, loginSession.getSyncOrUrl(), System.currentTimeMillis(),
                loginSession.getSKey(), loginSession.getWxSid(), loginSession.getWxUin(), deviceId, loginSession.getSyncKeyStr(), System.currentTimeMillis());
        checkUrl = checkUrl.replace("@", "%40").replace("|", "%7C");
        log.info("心跳地址:{}", checkUrl);
        String result = HttpClientUtil.httpGet(checkUrl);
        SyncCheckRet syncCheckRet = JSONObject.parseObject(result.replace("window.synccheck=", ""), SyncCheckRet.class);
        log.info("心跳返回: {}", result);
        //window.synccheck={retcode:"1100",selector:"0"}
        return syncCheckRet;
    }

    public static String wxStatusNotify(Map<String, Object> initMap, String userName) {
        String deviceId = "e" + WxUtil.getNormalRandom(15);
        String json = "{\"BaseRequest\":{\"Uin\":\"UIN\",\"Sid\":\"SID\",\"Skey\":\"SKEY\",\"DeviceID\":\"DEVICEID\"},\"Code\":3,\"FromUserName\":\"NAME\",\"ToUserName\":\"NAME\",\"ClientMsgId\":\"TIME\"}";
        json = json.replace("UIN", initMap.get("wxuin").toString()).replace("SID", initMap.get("wxsid").toString()).replace("SKEY", initMap.get("skey").toString())
                .replace("DEVICEID", deviceId).replace("NAME", userName).replace("TIME", String.valueOf(System.currentTimeMillis()));
        String notifyUrl = UrlConstants.notifyUrl.replace("PASSTICKET", initMap.get("pass_ticket").toString());
        String result = HttpClientUtil.jsonPost(notifyUrl, json, "UTF-8");
        System.out.println(result);
        return result;
    }

    /**
     * 根据扫码结果判断是否登录成功
     * 处理登录成功结果，写入loginSession对象
     *
     * @param qrocodeResult//扫码结果
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    public static LoginSession doLoginSession(String qrocodeResult) throws JDOMException, IOException {
        int start = qrocodeResult.indexOf("https");
        String newLoginUrl = qrocodeResult.substring(start).replace("\";", "");
        newLoginUrl += "&fun=new&version=v2";
        log.info("手机确认登录，检查登录地址:{}", newLoginUrl);
        String syncCheckDomain = WxUtil.getSyncCheckDomain(newLoginUrl);
        LoginSession loginSession = new LoginSession();
        loginSession.setSyncUrl(syncCheckDomain);
        String checkLoginResult = HttpClientUtil.httpGet(newLoginUrl);
        log.info("检查登录结果为{}", checkLoginResult);

        Map<String, Object> initMap = NodeUtil.doXMLParse(checkLoginResult);
        if (initMap.get("ret").equals("0")) {
            log.info("登录成功");
            loginSession.setSuccess(true);
            loginSession.setPassTicket(initMap.get("pass_ticket").toString());
            loginSession.setWxSid(initMap.get("wxsid").toString());
            loginSession.setWxUin(initMap.get("wxuin").toString());
            loginSession.setSKey(initMap.get("skey").toString());
            //初始化用户信息
            loginSession = init(loginSession);
            String notifyResult = WxUtil.wxStatusNotify(initMap, loginSession.getUser().getUserName());
            log.info("开启消息通知,{}", notifyResult);
            return loginSession;
        }
        log.error("登录失败");
        loginSession.setSuccess(false);
        return loginSession;
    }

    private static LoginSession init(LoginSession loginSession) {
        String initUrl = String.format(UrlConstants.initUrl, loginSession.getPassTicket());
        log.info("initURL " + initUrl);
        BaseRequest baseRequest = WxUtil.getBaseRequest(loginSession);
        loginSession.setBaseRequest(baseRequest);
        String initResult = HttpClientUtil.jsonPost(initUrl, WxUtil.getInitJson(loginSession), "UTF-8");
        log.info("initResult  " + initResult);
        //解析初始化信息
        InitResponse initResponse = JSONObject.parseObject(initResult, InitResponse.class);
        if (initResponse.getBaseResponse().getRet().equals(0)) {
            log.info("用户好友列表初始化成功");
            loginSession.setUser(initResponse.getUser());
            loginSession.setSyncKey(initResponse.getSyncKey());
            loginSession.setContactList(initResponse.getContactList());
        } else {
            log.error("用户好友列表初始化失败");
            loginSession.setSuccess(false);
        }
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


    /**
     * 处理监听到的消息
     *
     * @param sync
     * @return
     */
    public static List<WeChatMessage> processMsg(LoginSession loginSession, WebSyncResponse sync) {
        List<Message> messages = sync.getAddMessageList();
        if (null != messages && messages.size() > 0) {
            List<WeChatMessage> weChatMessages = new ArrayList<>(messages.size());
            boolean hashNewMsg = false;
            for (Message message : messages) {
                WeChatMessage weChatMessage = processMsgDetail(loginSession, message);
                if (null != weChatMessage) {
                    weChatMessages.add(weChatMessage);
                    hashNewMsg = true;
                }
            }
            if (hashNewMsg) {
                log.info("你有新的疑似红包消息");
            }
            return weChatMessages;
        }
        return null;
    }


    public static String formatMsg(String msg) {
        msg = msg.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("<br/>", "\n");
        return msg;
    }


    private static WeChatMessage processMsgDetail(LoginSession loginSession, Message message) {
        Integer msgType = message.getType();//消息类型
        String name = getUserRemarkName(loginSession, message.getFromUserName());
        String msgId = message.getId();
        String content = message.getContent();


        // 不处理自己发的消息
        if (message.getFromUserName().equals(loginSession.getUser().getUserName())) {
            return null;
        }

        WeChatMessage.WeChatMessageBuilder weChatMessageBuilder = WeChatMessage.builder()
                .raw(message)
                .id(message.getId())
                .fromUserName(message.getFromUserName())
                .toUserName(message.getToUserName())
                .mineUserName(loginSession.getUser().getUserName())
                .mineNickName(loginSession.getUser().getNickName())
                .msgType(message.msgType())
                .text(content);


        //当前只监听红包消息
        if (message.msgType().equals(MsgType.SHARE)) {
            content = formatMsg(content);
            log.info("消息类型为{},来自{},消息内容为{}", msgType, name, content);
            return weChatMessageBuilder.text(content).build();
        }
        log.info("消息类型为{},来自{},消息内容为{}", msgType, name, content);
        return null;
    }


    private static String getUserRemarkName(LoginSession loginSession, String id) {
        String name = id.contains("@@") ? "未知群" : "陌生人";
        if (id.equals(loginSession.getUser().getUserName())) {
            return loginSession.getUser().getNickName();
        }
        List<Account> accounts = loginSession.getContactList().stream().filter(acc -> acc.getUserName().equals(id)).collect(Collectors.toList());
        if (null == accounts || accounts.size() == 0) {
            return name;
        }
        Account account = accounts.get(0);
        String nickName = StringUtils.isNotEmpty(account.getRemarkName()) ? account.getRemarkName() : account.getNickName();
        return StringUtils.isNotEmpty(nickName) ? nickName : name;
    }

    public static void main(String[] args) {
        List<Account> aaa = new ArrayList<>();
        Account account = new Account();
        account.setUserName("一");
        aaa.add(account);

        account = new Account();
        account.setUserName("二");
        aaa.add(account);

        account = new Account();
        account.setUserName("三");
        aaa.add(account);
        System.out.println(aaa.size());
        aaa.forEach(System.out::println);
        List<Account> ccc = aaa.stream().filter(bbb -> "一".equals(bbb.getUserName())).collect(Collectors.toList());
        System.out.println(ccc.size());
        System.out.println(ccc.get(0));

    }

}
