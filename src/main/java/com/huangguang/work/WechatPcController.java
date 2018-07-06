package com.huangguang.work;

import com.huangguang.work.entity.LoginSession;
import com.huangguang.work.service.DemoService;
import com.huangguang.work.util.HttpClientUtil;
import com.huangguang.work.util.PaymentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.management.LockInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:网页微信
 * User : huangguang
 * DATE : 2018-06-21 16:36
 */
@Slf4j
@Controller
@RequestMapping("pcwx")
public class WechatPcController {

    @Autowired
    private DemoService demoService;

    @RequestMapping("qcrode")
    public Object getQcrode(Model model, HttpServletRequest request, HttpServletResponse response) {
        String uuid = demoService.getUUID();
        if (StringUtils.isBlank(uuid)) {
            model.addAttribute("qcrodeUrl", "/static/404.gif");
        } else {
            model.addAttribute("qcrodeUrl", demoService.getQcrodeUrl(uuid));
            model.addAttribute("uuid", uuid);
        }
        //context.publishEvent(uuid);
        return "index";
    }

    @RequestMapping("listen")
    @ResponseBody
    public Map<String, Object> loginListen(String uuid) {
        return demoService.loginListen(uuid);
    }

    @RequestMapping("syncCheckListen")
    @ResponseBody
    public Map<String, Object> syncCheckListen(@RequestBody LoginSession loginSession) {
        return demoService.syncCheck(loginSession);
    }

    @RequestMapping("order")
    @ResponseBody
    public Object getOrderHtml(String orderId) {
        String url = "http://hgdp.nat.58xiangjian.com/wp_pay_3.0/pcwx/test";
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId);
        String html = PaymentUtil.createAutoFormHtml(url, params, "UTF-8");
        return html;
    }

    @RequestMapping("test")
    public Object test(Model model) {
        System.out.println("显示我");
        model.addAttribute("orderId", "aaaaaaaaaaaa");
        return "order";
    }
}
