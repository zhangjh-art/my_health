package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class ReservationConfigRespVO {

    private Long id;

    private Long userId;

    private Integer autoConfirm;

    @JsonSerialize(using = DateSerializer.class)
    private Date expireDate;

    private Integer intervalTime;

    private Integer intervalNum;

    private Integer advanceTime;

    private Map<Integer, List<Map<String, String>>> timeConfig;
}
