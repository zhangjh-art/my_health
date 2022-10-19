package com.cnasoft.health.userservice.excel.bean;

import java.util.regex.Pattern;

public class MyRegularExpression {
    public static final String EMAIL ="^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    public static final String MOBILE ="^[1][3-9]\\d{9}$";
    public static final String IDRE18 ="^([1-6][1-9]|50)\\d{4}(18|19|20)\\d{2}((0[1-9])|10|11|12)(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
    public static final String IDRE15 ="^([1-6][1-9]|50)\\d{4}\\d{2}((0[1-9])|10|11|12)(([0-2][1-9])|10|20|30|31)\\d{3}$";

    public static boolean IDValid(String value){
        boolean a =  Pattern.matches(IDRE18, value);
        if(!a){
            a =  Pattern.matches(IDRE15, value);
        }
        return a;
    }


}
