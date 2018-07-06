package com.huangguang.work.util;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.*;

/**
 * Created by huangguang on 2017/5/12.
 */
public class PaymentUtil {

    private static final String[] hexDigits = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
    /**
     * 解析请求参数
     * @param req
     * @return
     */
    public static Map<String, String> request2Map(HttpServletRequest req) {
        Map<String, String> map = new HashMap<String, String>();
        @SuppressWarnings("unchecked")
        Enumeration<String> enums = req.getParameterNames();
        while (enums.hasMoreElements()) {
            String name = enums.nextElement();
            String value = req.getParameter(name);
            if (req.getParameterValues(name) != null) {
                value = StringUtils.join(req.getParameterValues(name), ",");
            }
            map.put(name, value);
        }
        return map;
    }

    /**
     * 解析请求参数,将value值做URLDecoder.decode(value, "UTF-8")处理
     * @param req
     * @return
     */
    public static Map<String, String> paserReqToMap(HttpServletRequest req) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<String, String>();
        @SuppressWarnings("unchecked")
        Enumeration<String> enums = req.getParameterNames();
        while (enums.hasMoreElements()) {
            String name = enums.nextElement();
            String value = req.getParameter(name);
            if (req.getParameterValues(name) != null) {
                value = StringUtils.join(req.getParameterValues(name), ",");
            }
            map.put(name, URLDecoder.decode(value, "UTF-8"));
        }
        return map;
    }

    /**
     * 将形如"aaa=bbb&&ccc=ddd&sss=ttt"的返回字符串解析成map
     * @param respStr
     * @return
     */
    public static Map<String, String> paserStrToMap(String respStr) {
        Map<String, String> map = new HashMap<String, String>();
        if (StringUtils.isNotEmpty(respStr)) {
            String[] strs = respStr.split("&");
            for (String str : strs) {
                if (StringUtils.isEmpty(str)) {
                    continue;
                }
                int index = str.indexOf("=");
                map.put(str.substring(0, index), str.substring(index + 1));
            }
        }
        return map;
    }

    public static String parseMapToStr(Map<String, Object> map) {
        StringBuffer sb = new StringBuffer();
        for(Map.Entry<String, Object> e : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.toString();
    }

    public static String createSign(String characterEncoding, SortedMap<Object, Object> parameters, String secretKey) {
        StringBuffer sb = new StringBuffer();
        Set<?> es = parameters.entrySet();
        Iterator it = es.iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String key = (String)entry.getKey();
            Object value = entry.getValue();
            if(null != value && !"".equals(value) && !"sign".equals(key) && !"key".equals(key)) {
                sb.append(key + "=" + value + "&");
            }
        }
        sb.append("key=" + secretKey);
        String sign = MD5Encode(sb.toString(), characterEncoding).toUpperCase();
        return sign;
    }

    public static String MD5Encode(String origin, String charsetname) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if(charsetname != null && !"".equals(charsetname)) {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
            } else {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            }
        } catch (Exception var4) {
            ;
        }
        return resultString;
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for(int i = 0; i < b.length; ++i) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if(b < 0) {
            n = b + 256;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }


    /**
     * 将Map生成XML
     *
     * @param parameters
     * @return String
     */
    @SuppressWarnings("rawtypes")
    public static String getRequestXml(SortedMap<Object, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set<?> es = parameters.entrySet();
        Iterator<?> it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v ="";
            if (k.equals("total_fee")) {
                v = (String)entry.getValue();
            } else {
                v = (String) entry.getValue();
            }
            sb.append("<" + k + ">" + v + "</" + k + ">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    public static Map<String, Object> doXMLParse(String strxml) throws JDOMException, IOException {
        strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");
        if(null != strxml && !"".equals(strxml)) {
            Map<String, Object> m = new HashMap();
            InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(in);
            Element root = doc.getRootElement();
            List<Element> list = root.getChildren();

            String k;
            String v;
            for(Iterator it = list.iterator(); it.hasNext(); m.put(k, v)) {
                Element e = (Element)it.next();
                k = e.getName();
                v = "";
                List<Element> children = e.getChildren();
                if(children.isEmpty()) {
                    v = e.getTextNormalize();
                } else {
                    v = getChildrenText(children);
                }
            }

            in.close();
            return m;
        } else {
            return null;
        }
    }

    private static String getChildrenText(List<Element> children) {
        StringBuffer sb = new StringBuffer();
        if (!children.isEmpty()) {
            Iterator it = children.iterator();
            while (it.hasNext()) {
                Element e = (Element) it.next();
                String name = e.getName();
                String value = e.getTextNormalize();
                List<Element> list = e.getChildren();
                sb.append("<" + name + ">");
                if (!list.isEmpty()) {
                    sb.append(getChildrenText(list));
                }

                sb.append(value);
                sb.append("</" + name + ">");
            }
        }
        return sb.toString();
    }

    public static boolean isTenpaySign(String characterEncoding, SortedMap<Object, Object> packageParams, String secretKey) {
        StringBuffer sb = new StringBuffer();
        Set<Map.Entry<Object, Object>> es = packageParams.entrySet();
        Iterator it = es.iterator();

        String k;
        while(it.hasNext()) {
            Map.Entry<Object, Object> entry = (Map.Entry)it.next();
            k = (String)entry.getKey();
            String v = (String)entry.getValue();
            if(!"sign".equals(k) && null != v && !"".equals(v)) {
                sb.append(k + "=" + v + "&");
            }
        }

        sb.append("key=" + secretKey);
        String mysign = MD5Encode(sb.toString(), characterEncoding).toLowerCase();
        k = ((String)packageParams.get("sign")).toLowerCase();
        return k.equals(mysign);
    }

    public static String getInfoFromRequest(HttpServletRequest request) {
        StringBuffer info = new StringBuffer();
        ServletInputStream in = null;
        try {
            in = request.getInputStream();
            BufferedInputStream buf = new BufferedInputStream(in);
            byte[] buffer = new byte[1024];

            int iRead;
            while((iRead = buf.read(buffer)) != -1) {
                info.append(new String(buffer, 0, iRead, "UTF-8"));
            }
            String var6 = URLDecoder.decode(info.toString(), "UTF-8");
            return var6;
        } catch (Exception var15) {
            var15.printStackTrace();
            throw new RuntimeException("接收 request 参数异常", var15);
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException var14) {
                    var14.printStackTrace();
                }
            }

        }
    }

    /**
     * 功能：前台交易构造HTTP POST自动提交表单<br>
     * @param action 表单提交地址<br>
     * @param hiddens 以MAP形式存储的表单键值<br>
     * @param encoding 上送请求报文域encoding字段的值<br>
     * @return 构造好的HTTP POST交易表单<br>
     */
    public static String createAutoFormHtml(String action, Map<String, String> hiddens, String encoding) {
        StringBuffer sf = new StringBuffer();
        sf.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset="+encoding+"\"/></head><body>");
        sf.append("<form id = \"pay_form\" action=\"" + action
                + "\" method=\"post\">");
        if (null != hiddens && 0 != hiddens.size()) {
            Set<Map.Entry<String, String>> set = hiddens.entrySet();
            Iterator<Map.Entry<String, String>> it = set.iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> ey = it.next();
                String key = ey.getKey();
                String value = ey.getValue();
                sf.append("<input type=\"hidden\" name=\"" + key + "\" id=\""
                        + key + "\" value=\"" + value + "\"/>");
            }
        }
        sf.append("</form>");
        sf.append("</body>");
        sf.append("<script type=\"text/javascript\">");
        sf.append("document.all.pay_form.submit();");
        sf.append("</script>");
        sf.append("</html>");
        return sf.toString();
    }

    /**
     * 有效期为当天
     * @return
     */
    public static int dayOfValidity () {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        int second= subSecond(cal.getTime(), new Date());
        if(second<=0){
            second=1;
        }
        return second;
    }

    /**
     * 返回date1-dat2相差的秒数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int subSecond(Date date1, Date date2) {
        long d1 = date1.getTime();
        long d2 = date2.getTime();
        int sub = (int) ((d1 - d2) / 1000);
        return sub;
    }

    /**
     * 指定长度的随机字符串
     * @param length
     * @return
     */
    public static String CreateNoncestr(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String res = "";
        for (int i = 0; i < length; i++) {
            Random rd = new Random();
            res += chars.indexOf(rd.nextInt(chars.length() - 1));
        }
        return res;
    }

    public static String CreateNoncestr() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String res = "";
        for (int i = 0; i < 16; i++) {
            Random rd = new Random();
            res += chars.charAt(rd.nextInt(chars.length() - 1));
        }
        return res;
    }

    public static void main(String[] args) {
        BigDecimal a = new BigDecimal(10.22332);
        BigDecimal b = new BigDecimal(100);
        BigDecimal c = a.setScale(2, BigDecimal.ROUND_HALF_UP).multiply(b);
        System.out.println(String.valueOf(c.intValue()));
    }

    /**
     * 将封装的唤起微信支付的数据转换成String
     * @param respMap
     * @return
     */
    public static String convertPackageParam(Map<String, Object> respMap) {
        StringBuffer buffer = new StringBuffer();
        respMap.forEach((k,v) -> {
            buffer.append(k + "=" + v.toString() + "&");
        });
        return buffer.toString();
    }
}
