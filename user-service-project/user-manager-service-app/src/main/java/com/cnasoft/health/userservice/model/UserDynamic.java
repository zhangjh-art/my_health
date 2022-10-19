package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户动态
 *
 * @author Administrator
 */
@Data
@TableName("user_dynamic")
@EqualsAndHashCode(callSuper = false)
public class UserDynamic extends SuperModel<UserDynamic> {

    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 昵称
     */
    private String userNickName;

    /**
     * 心情内容
     */
    private String content;

    /**
     * 入睡时间
     */
    private Date sleepTime;

    /**
     * 起床时间
     */
    private Date getupTime;

    /**
     * 睡眠时长
     */
    private Integer sleepMinute;

    /**
     * 身高
     */
    private Double height;

    /**
     * 体重
     */
    private Double weight;

    /**
     * 是否小憩，1：是；0：否
     */
    private Integer nap;

    /**
     * 小憩时间
     */
    private Integer napMinute = 30;

    /**
     * 是否置顶 1是
     */
    private Integer sort = 0;

    /**
     * 是否预警
     */
    private Integer isWarn;

    /**
     * 预警等级
     */
    private Integer warnGrade;

    /**
     * 预警关键字
     */
    private String warnWords;

    /**
     * 处置结果：0：未处置；1：已处置；2：已面诊；3：已转诊；4：错误预警
     */
    private Integer dealResult;

    /**
     * 心情1开心2难过3大哭4高兴5悲伤
     */
    private Integer mood;

    /**
     * 处置描述
     */
    private String dealDescription;


    public UserDynamic() {
    }

    public UserDynamic(Long id, Long userId, String content, Date sleepTime, Date getupTime, int sleepMinute, double height, double weight, int nap, int napMinute, int sort, int isWarn, int warnGrade, String warnWords, int dealResult, String dealDescription) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.sleepTime = sleepTime;
        this.getupTime = getupTime;
        this.sleepMinute = sleepMinute;
        this.height = height;
        this.weight = weight;
        this.nap = nap;
        this.napMinute = napMinute;
        this.sort = sort;
        this.isWarn = isWarn;
        this.warnGrade = warnGrade;
        this.warnWords = warnWords;
        this.dealResult = dealResult;
        this.dealDescription = dealDescription;
    }
}
