package com.cnasoft.health.userservice.feign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * PAD版本升级信息
 *
 * @author Administrator
 * @date 2022/9/2 20:52
 **/
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class UpgradeDTO {

    /**
     * 最新版本号
     */
    private Integer version;

    /**
     * APP下载地址
     */
    private String downloadUrl;

    /**
     * 是否强制升级：false不强制升级，true强制升级
     */
    private Boolean forceUpgrade;

    /**
     * 升级描述
     */
    private String upgradeDescription;
}
