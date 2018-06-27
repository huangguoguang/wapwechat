package com.huangguang.work.util;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-25 15:59
 */
public class UrlConstants {

    /**
     * 检测登录地址
     */
    public static String newLoginUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage?ticket=AYfON5sIiI-wazwcZJIxp0Oz@qrticket_0&uuid=UUID&lang=zh_CN&scan=1529982666&fun=new&version=v2";

    /**
     * 首页信息初始化地址
     */
    public static String initUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxinit?r=-990263531&lang=zh_CN&pass_ticket=PASSTICKET";//POST

    /**
     * 监听信息url
     */
    public static String syncChekUrl = "https://PHSHDOMAINNAME/cgi-bin/mmwebwx-bin/synccheck?r=TIMESTAMP1&skey=SKEY&sid=SID&uin=UIN&deviceid=DEVICEID&synckey=SYNCKEY&_=TIMESTAMP";

    /**
     * 接收信息url
     */
    public static String syncUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsync?sid=SID&skey=SKEY&lang=zh_CN&pass_ticket=PASSTICKET";



    public static String domainName = "wx.qq.com";
    public static String pushDomainName = "webpush.weixin.qq.com";
    public static String pushDomainName2 = "webpush2.weixin.qq.com";
}
