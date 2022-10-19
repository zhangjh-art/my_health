package com.cnasoft.health.userservice.constant;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 学生相关常量
 *
 * @author zcb
 */
public interface StudentConstant {

    /**
     * 导入模板/自定义模板处理中
     */
    Integer PROCESSING = 1;

    /**
     * 导入模板/自定义模板处理完毕
     */
    Integer FINISHED = 2;

    //国籍
    Set<String> COUNTRIES = Stream.of("中国", "其他").collect(Collectors.toCollection(HashSet::new));
    //是或者否
    Set<String> YES_NOT = Stream.of("是", "否").collect(Collectors.toCollection(HashSet::new));
    //民族
    Set<String> NATIONS = Stream.of("汉族", "壮族", "回族", "满族", "维吾尔族", "苗族", "彝族", "土家族", "藏族", "蒙古族", "其他").collect(Collectors.toCollection(HashSet::new));
    //政治面貌
    Set<String> POLITICS_STATUS = Stream.of("中共党员", "共青团员", "民主党派", "群众", "其他").collect(Collectors.toCollection(HashSet::new));
    //家中排行
    Set<String> FAMILY_SORT = Stream.of("大子女", "中间子女", "最小子女").collect(Collectors.toCollection(HashSet::new));
    //血型
    Set<String> BLOOD_TYPE = Stream.of("A型", "B型", "O型", "AB型").collect(Collectors.toCollection(HashSet::new));
    //健康状况
    Set<String> HEALTHY_STATUS = Stream.of("未填", "健康或良好", "一般或较弱", "有慢性病", "有生理缺陷", "残疾", "其他").collect(Collectors.toCollection(HashSet::new));
    //残疾类型
    Set<String> DISABILITY_TYPE = Stream.of("无残疾", "视力残疾", "听力残疾", "言语残疾", "肢体残疾", "智力残疾", "精神残疾").collect(Collectors.toCollection(HashSet::new));
    //特殊情况分类
    Set<String> SPECIAL_CONDITION = Stream.of("辍学", "复读", "贫困生", "家庭情况特殊", "其他").collect(Collectors.toCollection(HashSet::new));
}
