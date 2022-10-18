package com.cnasoft.health.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author ganghe
 * @date 2022/4/9 15:38
 **/
@Slf4j
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtils() {
    }

    /**
     * Singleton of Jackson Object Mapper
     *
     * @return ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 对象转json
     *
     * @param object 对象
     * @return 字符串
     */
    public static String writeValueAsString(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Jackson转换字符串失败", e);
        }
        return null;
    }

    /**
     * 对象转json
     *
     * @param object 对象
     * @return 字符串
     */
    public static String writeValueAsStringExcludeNull(Object object) {
        try {
            OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Jackson转换字符串失败", e);
        }
        return null;
    }

    /**
     * json转对象
     *
     * @param json      字符串
     * @param valueType 对象
     * @return 对象
     */
    public static <T> T readValue(String json, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            log.error("Jackson转换对象失败", e);
        }
        return null;
    }

    /**
     * json转对象
     *
     * @param p
     * @param valueType
     * @param <T>
     * @return
     */
    public static <T> T readValue(JsonParser p, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(p, valueType);
        } catch (IOException e) {
            log.error("Jackson转换对象失败", e);
        }
        return null;
    }

    /**
     * json转对象
     *
     * @param json
     * @param valueTypeRef
     * @param <T>
     * @return
     */
    public static <T> T readValue(String json, TypeReference<T> valueTypeRef) {
        try {
            return OBJECT_MAPPER.readValue(json, valueTypeRef);
        } catch (JsonProcessingException e) {
            log.error("Jackson转换对象失败", e);
        }
        return null;
    }

    /**
     * json转对象
     *
     * @param p
     * @param valueTypeRef
     * @param <T>
     * @return
     */
    public static <T> T readValue(JsonParser p, TypeReference<T> valueTypeRef) {
        try {
            return OBJECT_MAPPER.readValue(p, valueTypeRef);
        } catch (IOException e) {
            log.error("Jackson转换对象失败", e);
        }
        return null;
    }

    public static byte[] writeValueAsBytes(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            log.error("Jackson对象转Byte失败", e);
        }
        return null;
    }
}