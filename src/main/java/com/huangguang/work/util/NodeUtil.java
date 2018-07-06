package com.huangguang.work.util;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-22 18:14
 */
public class NodeUtil {
    /**
     * 将XML字符串解析成MAP
     *
     * @param strxml
     * @return Map
     * @throws JDOMException
     * @throws IOException
     */
    public static Map<String, Object> doXMLParse(String strxml) throws JDOMException, IOException {
        if (null == strxml || "".equals(strxml)) {
            return null;
        }
        Map<String, Object> m = new HashMap<String, Object>();
        InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(in);
        Element root = doc.getRootElement();
        List<Element> list = root.getChildren();
        Iterator<?> it = list.iterator();
        while (it.hasNext()) {
            Element e = (Element) it.next();
            String k = e.getName();
            String v = "";
            List<Element> children = e.getChildren();
            if (children.isEmpty()) {
                v = e.getTextNormalize();
            } else {
                v = getChildrenText(children);
            }
            m.put(k, v);
        }
        in.close();
        return m;
    }

    /**
     * 获取子结点的xml
     *
     * @param children
     * @return String
     */
    private static String getChildrenText(List<Element> children) {
        StringBuffer sb = new StringBuffer();
        if (!children.isEmpty()) {
            Iterator<?> it = children.iterator();
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

    /**
     * 将xml解析成map
     * @param xml
     * @return
     * @throws IOException
     * @throws JDOMException
     */
    public static Map<String, Object> convertXMLToMap(String xml) throws IOException, JDOMException {
        xml = xml.replaceFirst("encoding = \".*\"", "encoding = \"UTF-8\"");
        if (null == xml || "".equals(xml)) {
            return null;
        }
        System.out.println(xml);
        Map<String, Object> map = new HashMap<String, Object>();
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(in);
        Element root = document.getRootElement();
        List<Element> list = root.getChildren();
        list.forEach(element -> {
            String key = element.getName();
            String value = "";
            List<Element> children = element.getChildren();
            if (!children.isEmpty()) {
                value = getChildrenText(map, children);
            } else {
                value = element.getTextNormalize();
                map.put(key, value);
            }
        });
        return map;
    }

    public static String getChildrenText(Map<String, Object> map, List<Element> children) {
        StringBuffer sb = new StringBuffer();
        if (!children.isEmpty()) {
            children.forEach(child -> {
                String key = child.getName();
                String value = "";
                if (!child.getChildren().isEmpty()) {
                    value = getChildrenText(map, child.getChildren());
                } else {
                    value = child.getTextNormalize();
                    map.put(key, value);
                }
            });
        }
        return sb.toString();
    }
}
