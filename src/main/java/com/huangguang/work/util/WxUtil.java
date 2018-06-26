package com.huangguang.work.util;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-25 15:55
 */
public class WxUtil {
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
}
