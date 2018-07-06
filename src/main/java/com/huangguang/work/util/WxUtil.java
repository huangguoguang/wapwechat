package com.huangguang.work.util;

import com.alibaba.fastjson.JSON;
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
            try {
                Map<String, Object> map = NodeUtil.convertXMLToMap(content);
                log.info("消息类型为{},来自{},消息内容为{}", msgType, name, content);
                log.info(map.get("appname").toString());
                log.info(map.get("des").toString());
                if (map.get("appname").equals("微信支付")) {
                    String des = map.get("des").toString();
                    System.out.println("收款金额  " + getSubString(des, "收款金额：￥", "付款方备注"));
                    System.out.println("付款方备注  " + getSubString(des, "付款方备注：", "汇总"));
                }
            } catch (Exception e) {
                log.error("解析消息异常");
            }
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

    public static String getSubString(String text, String left, String right) {
        String result = "";
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right == null || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }


    public static void main(String[] args) throws JDOMException, IOException {
        String msg = "{\"BaseResponse\": {\"Ret\": 0,\"ErrMsg\": \"\"},\"AddMsgCount\": 1,\"AddMsgList\": [{\"MsgId\": \"4286233522359858164\",\"FromUserName\": \"@ff802473e3aad986d24443a5d379203c\",\"ToUserName\": \"@1331d9e96d7108253a455d495e0614cb\",\"MsgType\": 49,\"Content\": \"&lt;msg&gt; &lt;appmsg appid=\\\"\\\" sdkver=\\\"0\\\"&gt; \\t&lt;title&gt;&lt;![CDATA[微信支付收款0.01元]]&gt;&lt;/title&gt; \\t&lt;des&gt;&lt;![CDATA[收款金额：￥0.01<br/>汇总：今日第1笔收款，共计￥0.01<br/>收款成功，已存入零钱。点击可查看详情]]&gt;&lt;/des&gt; \\t&lt;action&gt;&lt;/action&gt; \\t&lt;type&gt;5&lt;/type&gt; \\t&lt;showtype&gt;1&lt;/showtype&gt;     &lt;soundtype&gt;1&lt;/soundtype&gt; \\t&lt;content&gt;&lt;![CDATA[]]&gt;&lt;/content&gt; \\t&lt;contentattr&gt;0&lt;/contentattr&gt; \\t&lt;url&gt;&lt;![CDATA[https://payapp.weixin.qq.com/payf2f/jumpf2fbill?timestamp=1530522194&amp;openid=J2vRhUuzQuBsNcrTXkuuOzFFpYQmFwz_IJu1IYqYB_4=]]&gt;&lt;/url&gt; \\t&lt;lowurl&gt;&lt;![CDATA[]]&gt;&lt;/lowurl&gt; \\t&lt;appattach&gt; \\t\\t&lt;totallen&gt;0&lt;/totallen&gt; \\t\\t&lt;attachid&gt;&lt;/attachid&gt; \\t\\t&lt;fileext&gt;&lt;/fileext&gt; \\t\\t&lt;cdnthumburl&gt;&lt;![CDATA[]]&gt;&lt;/cdnthumburl&gt; \\t\\t&lt;cdnthumbaeskey&gt;&lt;![CDATA[]]&gt;&lt;/cdnthumbaeskey&gt; \\t\\t&lt;aeskey&gt;&lt;![CDATA[]]&gt;&lt;/aeskey&gt; \\t&lt;/appattach&gt; \\t&lt;extinfo&gt;&lt;/extinfo&gt; \\t&lt;sourceusername&gt;&lt;/sourceusername&gt; \\t&lt;sourcedisplayname&gt;&lt;![CDATA[]]&gt;&lt;/sourcedisplayname&gt; \\t&lt;mmreader&gt; \\t\\t&lt;category type=\\\"0\\\" count=\\\"1\\\"&gt; \\t\\t\\t&lt;name&gt;&lt;![CDATA[微信支付]]&gt;&lt;/name&gt; \\t\\t\\t&lt;topnew&gt; \\t\\t\\t\\t&lt;cover&gt;&lt;![CDATA[]]&gt;&lt;/cover&gt; \\t\\t\\t\\t&lt;width&gt;0&lt;/width&gt; \\t\\t\\t\\t&lt;height&gt;0&lt;/height&gt; \\t\\t\\t\\t&lt;digest&gt;&lt;![CDATA[收款金额：￥0.01<br/>汇总：今日第1笔收款，共计￥0.01<br/>收款成功，已存入零钱。点击可查看详情]]&gt;&lt;/digest&gt; \\t\\t\\t&lt;/topnew&gt; \\t\\t\\t\\t&lt;item&gt; \\t&lt;itemshowtype&gt;4&lt;/itemshowtype&gt; \\t&lt;title&gt;&lt;![CDATA[收款到账通知]]&gt;&lt;/title&gt; \\t&lt;url&gt;&lt;![CDATA[https://payapp.weixin.qq.com/payf2f/jumpf2fbill?timestamp=1530522194&amp;openid=J2vRhUuzQuBsNcrTXkuuOzFFpYQmFwz_IJu1IYqYB_4=]]&gt;&lt;/url&gt; \\t&lt;shorturl&gt;&lt;![CDATA[]]&gt;&lt;/shorturl&gt; \\t&lt;longurl&gt;&lt;![CDATA[]]&gt;&lt;/longurl&gt; \\t&lt;pub_time&gt;1530522194&lt;/pub_time&gt; \\t&lt;cover&gt;&lt;![CDATA[]]&gt;&lt;/cover&gt; \\t&lt;tweetid&gt;&lt;/tweetid&gt; \\t&lt;digest&gt;&lt;![CDATA[收款金额：￥0.01<br/>汇总：今日第1笔收款，共计￥0.01<br/>收款成功，已存入零钱。点击可查看详情]]&gt;&lt;/digest&gt; \\t&lt;fileid&gt;0&lt;/fileid&gt; \\t&lt;sources&gt; \\t&lt;source&gt; \\t&lt;name&gt;&lt;![CDATA[微信支付]]&gt;&lt;/name&gt; \\t&lt;/source&gt; \\t&lt;/sources&gt; \\t&lt;styles&gt;&lt;topColor&gt;&lt;![CDATA[]]&gt;&lt;/topColor&gt;<br/>&lt;style&gt;<br/>&lt;range&gt;&lt;![CDATA[{5,5}]]&gt;&lt;/range&gt;<br/>&lt;font&gt;&lt;![CDATA[s]]&gt;&lt;/font&gt;<br/>&lt;color&gt;&lt;![CDATA[#000000]]&gt;&lt;/color&gt;<br/>&lt;/style&gt;<br/>&lt;style&gt;<br/>&lt;range&gt;&lt;![CDATA[{14,15}]]&gt;&lt;/range&gt;<br/>&lt;font&gt;&lt;![CDATA[s]]&gt;&lt;/font&gt;<br/>&lt;color&gt;&lt;![CDATA[#000000]]&gt;&lt;/color&gt;<br/>&lt;/style&gt;<br/>&lt;style&gt;<br/>&lt;range&gt;&lt;![CDATA[{30,18}]]&gt;&lt;/range&gt;<br/>&lt;font&gt;&lt;![CDATA[s]]&gt;&lt;/font&gt;<br/>&lt;color&gt;&lt;![CDATA[#000000]]&gt;&lt;/color&gt;<br/>&lt;/style&gt;<br/>&lt;/styles&gt;\\t&lt;native_url&gt;&lt;/native_url&gt;    &lt;del_flag&gt;0&lt;/del_flag&gt;     &lt;contentattr&gt;0&lt;/contentattr&gt;     &lt;play_length&gt;0&lt;/play_length&gt; \\t&lt;play_url&gt;&lt;![CDATA[]]&gt;&lt;/play_url&gt; \\t&lt;player&gt;&lt;![CDATA[]]&gt;&lt;/player&gt; \\t&lt;template_op_type&gt;1&lt;/template_op_type&gt; \\t&lt;weapp_username&gt;&lt;![CDATA[gh_fac0ad4c321d@app]]&gt;&lt;/weapp_username&gt; \\t&lt;weapp_path&gt;&lt;![CDATA[pages/index/index.html]]&gt;&lt;/weapp_path&gt; \\t&lt;weapp_version&gt;102&lt;/weapp_version&gt; \\t&lt;weapp_state&gt;0&lt;/weapp_state&gt;     &lt;music_source&gt;0&lt;/music_source&gt;     &lt;pic_num&gt;0&lt;/pic_num&gt; \\t&lt;show_complaint_button&gt;0&lt;/show_complaint_button&gt; \\t&lt;/item&gt; \\t\\t&lt;/category&gt; \\t\\t&lt;publisher&gt; \\t\\t\\t&lt;username&gt;&lt;/username&gt; \\t\\t\\t&lt;nickname&gt;&lt;![CDATA[微信支付]]&gt;&lt;/nickname&gt; \\t\\t&lt;/publisher&gt; \\t\\t&lt;template_header&gt;&lt;title&gt;&lt;![CDATA[收款到账通知]]&gt;&lt;/title&gt;<br/>&lt;title_color&gt;&lt;![CDATA[]]&gt;&lt;/title_color&gt;<br/>&lt;pub_time&gt;1530522194&lt;/pub_time&gt;<br/>&lt;first_data&gt;&lt;![CDATA[]]&gt;&lt;/first_data&gt;<br/>&lt;first_color&gt;&lt;![CDATA[]]&gt;&lt;/first_color&gt;<br/>&lt;/template_header&gt; \\t\\t&lt;template_detail&gt;&lt;template_show_type&gt;1&lt;/template_show_type&gt;<br/>&lt;text_content&gt;<br/>&lt;cover&gt;&lt;![CDATA[]]&gt;&lt;/cover&gt;<br/>&lt;text&gt;&lt;![CDATA[]]&gt;&lt;/text&gt;<br/>&lt;color&gt;&lt;![CDATA[]]&gt;&lt;/color&gt;<br/>&lt;/text_content&gt;<br/>&lt;line_content&gt;<br/>&lt;topline&gt;<br/>&lt;key&gt;<br/>&lt;word&gt;&lt;![CDATA[收款金额]]&gt;&lt;/word&gt;<br/>&lt;color&gt;&lt;![CDATA[#888888]]&gt;&lt;/color&gt;<br/>&lt;/key&gt;<br/>&lt;value&gt;<br/>&lt;word&gt;&lt;![CDATA[￥0.01]]&gt;&lt;/word&gt;<br/>&lt;color&gt;&lt;![CDATA[#000000]]&gt;&lt;/color&gt;<br/>&lt;/value&gt;<br/>&lt;/topline&gt;<br/>&lt;lines&gt;<br/>&lt;line&gt;<br/>&lt;key&gt;<br/>&lt;word&gt;&lt;![CDATA[汇总]]&gt;&lt;/word&gt;<br/>&lt;color&gt;&lt;![CDATA[#888888]]&gt;&lt;/color&gt;<br/>&lt;/key&gt;<br/>&lt;value&gt;<br/>&lt;word&gt;&lt;![CDATA[今日第1笔收款，共计￥0.01]]&gt;&lt;/word&gt;<br/>&lt;color&gt;&lt;![CDATA[#000000]]&gt;&lt;/color&gt;<br/>&lt;/value&gt;<br/>&lt;/line&gt;<br/>&lt;line&gt;<br/>&lt;key&gt;<br/>&lt;word&gt;&lt;![CDATA[备注]]&gt;&lt;/word&gt;<br/>&lt;color&gt;&lt;![CDATA[#888888]]&gt;&lt;/color&gt;<br/>&lt;/key&gt;<br/>&lt;value&gt;<br/>&lt;word&gt;&lt;![CDATA[收款成功，已存入零钱。点击可查看详情]]&gt;&lt;/word&gt;<br/>&lt;color&gt;&lt;![CDATA[#000000]]&gt;&lt;/color&gt;<br/>&lt;/value&gt;<br/>&lt;/line&gt;<br/>&lt;/lines&gt;<br/>&lt;/line_content&gt;<br/>&lt;opitems&gt;<br/>&lt;opitem&gt;<br/>&lt;word&gt;&lt;![CDATA[查看详情]]&gt;&lt;/word&gt;<br/>&lt;url&gt;&lt;![CDATA[https://payapp.weixin.qq.com/payf2f/jumpf2fbill?timestamp=1530522194&amp;openid=J2vRhUuzQuBsNcrTXkuuOzFFpYQmFwz_IJu1IYqYB_4=]]&gt;&lt;/url&gt;<br/>&lt;icon&gt;&lt;![CDATA[]]&gt;&lt;/icon&gt;<br/>&lt;color&gt;&lt;![CDATA[#000000]]&gt;&lt;/color&gt;<br/>&lt;weapp_username&gt;&lt;![CDATA[gh_fac0ad4c321d@app]]&gt;&lt;/weapp_username&gt;<br/>&lt;weapp_path&gt;&lt;![CDATA[pages/index/index.html]]&gt;&lt;/weapp_path&gt;<br/>&lt;op_type&gt;1&lt;/op_type&gt;<br/>&lt;weapp_version&gt;102&lt;/weapp_version&gt;<br/>&lt;weapp_state&gt;0&lt;/weapp_state&gt;<br/>&lt;hint_word&gt;&lt;![CDATA[]]&gt;&lt;/hint_word&gt;<br/>&lt;/opitem&gt;<br/>&lt;/opitems&gt;<br/>&lt;/template_detail&gt; \\t    &lt;forbid_forward&gt;0&lt;/forbid_forward&gt; \\t&lt;/mmreader&gt; \\t&lt;thumburl&gt;&lt;![CDATA[]]&gt;&lt;/thumburl&gt; \\t     &lt;template_id&gt;&lt;![CDATA[I2582u0ZYjM6RkN9HQKibT5REyJciqX7wE-dQXzXtVE]]&gt;&lt;/template_id&gt;                          \\t&lt;ext_pay_info&gt;&lt;pay_fee&gt;&lt;![CDATA[1]]&gt;&lt;/pay_fee&gt;<br/>&lt;pay_feetype&gt;&lt;![CDATA[1]]&gt;&lt;/pay_feetype&gt;<br/>&lt;pay_outtradeno&gt;&lt;![CDATA[9X2BgooIrZt_Z0rqel_npQSsteGRjO21BPJo3iXwQ_jWgUGZssXeLzrVZJLfOFwK]]&gt;&lt;/pay_outtradeno&gt;<br/>&lt;pay_type&gt;&lt;![CDATA[wx_f2f]]&gt;&lt;/pay_type&gt;<br/>&lt;/ext_pay_info&gt; &lt;/appmsg&gt;&lt;fromusername&gt;&lt;/fromusername&gt;&lt;appinfo&gt;&lt;version&gt;0&lt;/version&gt;&lt;appname&gt;&lt;![CDATA[微信支付]]&gt;&lt;/appname&gt;&lt;isforceupdate&gt;1&lt;/isforceupdate&gt;&lt;/appinfo&gt;&lt;/msg&gt;\",\"Status\": 3,\"ImgStatus\": 1,\"CreateTime\": 1530522194,\"VoiceLength\": 0,\"PlayLength\": 0,\"FileName\": \"微信支付收款0.01元\",\"FileSize\": \"0\",\"MediaId\": \"\",\"Url\": \"https://payapp.weixin.qq.com/payf2f/jumpf2fbill?timestamp=1530522194&amp;openid=J2vRhUuzQuBsNcrTXkuuOzFFpYQmFwz_IJu1IYqYB_4=\",\"AppMsgType\": 5,\"StatusNotifyCode\": 0,\"StatusNotifyUserName\": \"\",\"RecommendInfo\": {\"UserName\": \"\",\"NickName\": \"\",\"QQNum\": 0,\"Province\": \"\",\"City\": \"\",\"Content\": \"\",\"Signature\": \"\",\"Alias\": \"\",\"Scene\": 0,\"VerifyFlag\": 0,\"AttrStatus\": 0,\"Sex\": 0,\"Ticket\": \"\",\"OpCode\": 0},\"ForwardFlag\": 0,\"AppInfo\": {\"AppID\": \"\",\"Type\": 0},\"HasProductId\": 0,\"Ticket\": \"\",\"ImgHeight\": 0,\"ImgWidth\": 0,\"SubMsgType\": 0,\"NewMsgId\": 4286233522359858164,\"OriContent\": \"\",\"EncryFileName\": \"%E5%BE%AE%E4%BF%A1%E6%94%AF%E4%BB%98%E6%94%B6%E6%AC%BE0%2E01%E5%85%83\"}],\"ModContactCount\": 0,\"ModContactList\": [],\"DelContactCount\": 0,\"DelContactList\": [],\"ModChatRoomMemberCount\": 0,\"ModChatRoomMemberList\": [],\"Profile\": {\"BitFlag\": 0,\"UserName\": {\"Buff\": \"\"},\"NickName\": {\"Buff\": \"\"},\"BindUin\": 0,\"BindEmail\": {\"Buff\": \"\"},\"BindMobile\": {\"Buff\": \"\"},\"Status\": 0,\"Sex\": 0,\"PersonalCard\": 0,\"Alias\": \"\",\"HeadImgUpdateFlag\": 0,\"HeadImgUrl\": \"\",\"Signature\": \"\"},\"ContinueFlag\": 0,\"SyncKey\": {\"Count\": 8,\"List\": [{\"Key\": 1,\"Val\": 667288196},{\"Key\": 2,\"Val\": 667288567},{\"Key\": 3,\"Val\": 667288060},{\"Key\": 11,\"Val\": 667288186},{\"Key\": 201,\"Val\": 1530522194},{\"Key\": 203,\"Val\": 1530504197},{\"Key\": 1000,\"Val\": 1530521162},{\"Key\": 1001,\"Val\": 1530521234}]},\"SKey\": \"\",\"SyncCheckKey\": {\"Count\": 8,\"List\": [{\"Key\": 1,\"Val\": 667288196},{\"Key\": 2,\"Val\": 667288567},{\"Key\": 3,\"Val\": 667288060},{\"Key\": 11,\"Val\": 667288186},{\"Key\": 201,\"Val\": 1530522194},{\"Key\": 203,\"Val\": 1530504197},{\"Key\": 1000,\"Val\": 1530521162},{\"Key\": 1001,\"Val\": 1530521234}]}}\n" +
                "\n" +
                "\n";
        WebSyncResponse response = JSONObject.parseObject(msg, WebSyncResponse.class);
        String content = response.getAddMessageList().get(0).getContent();
        content = formatMsg(content);
        System.out.println(content);

        Map<String, Object> map = NodeUtil.convertXMLToMap(content);
        System.out.println(map);
        System.out.println(map.get("des"));
        System.out.println(map.get("appname"));
        System.out.println(map.get("digest"));
        String des = map.get("des").toString();
        System.out.println(getSubString(des, "收款金额：￥", "付款方备注"));
        System.out.println(getSubString(des, "付款方备注：", "汇总"));
    }

}
