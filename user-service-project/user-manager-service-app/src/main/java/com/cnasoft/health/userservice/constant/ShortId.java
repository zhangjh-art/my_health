package com.cnasoft.health.userservice.constant;

import org.springframework.stereotype.Component;


/**
 * 短id
 *
 * @author cnasoft
 * @date 2020/8/17 14:09
 */
@Component
public final class ShortId {

    /**
     * 获取当前登录用户的schoolId
     *
     * @return 学校ID
     */
    public static String[] getShortIdSuffix() {
        return new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    }


}
