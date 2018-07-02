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
     * 取uuid Url
     */
    public static String uuidUrl = "https://login.wx.qq.com/jslogin?appid=wx782c26e4c19acffb&redirect_uri=https%3A%2F%2Fwx.qq.com%2Fcgi-bin%2Fmmwebwx-bin%2Fwebwxnewloginpage&fun=new&lang=zh_CN&_=TIMESTAMP";


    /**
     * 登录二维码链接url
     */
    public static String qcrodeUrl = "https://login.weixin.qq.com/qrcode/%s";

    /**
     * 登录url
     */
    public static String loginUrl = "https://login.wx.qq.com/cgi-bin/mmwebwx-bin/login?loginicon=true&uuid=%s&tip=0&r=-643281714&_=%s";


    /**
     * 首页信息初始化地址
     */
    public static String initUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxinit?r=-990263531&lang=zh_CN&pass_ticket=%s";//POST

    /**
     * 开启微信状态通知
     */
    public static String notifyUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxstatusnotify?lang=zh_CN&pass_ticket=PASSTICKET";

    /**
     * 监听信息url
     */
    public static String syncChekUrl = "https://%s/cgi-bin/mmwebwx-bin/synccheck?r=%s&skey=%s&sid=%s&uin=%s&deviceid=%s&synckey=%s&_=%s";

    /**
     * 接收信息url
     */
    public static String syncUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsync?sid=%s&skey=%s&pass_ticket=%s";



    public static String domainName = "wx.qq.com";
    public static String checkDomain = "webpush.wx.qq.com";
    public static String checkDomain2 = "webpush2.wx.qq.com";
}
