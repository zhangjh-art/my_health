package com.cnasoft.health.userservice.util;


/**
 * 短信工具类
 *
 * @author cnasoft
 * @date 2020/8/17 14:09
 */
public final class SmsUtil {

    /**
     * 生成6位短信验证码
     *
     * @return
     */
    public static String generateVerifyCode() {
        int[] numbers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(numbers[(int) (Math.random() * 10)]);
        }
        return sb.toString();
    }
}
