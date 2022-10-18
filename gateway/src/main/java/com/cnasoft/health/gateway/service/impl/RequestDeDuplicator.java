package com.cnasoft.health.gateway.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cnasoft.health.common.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * 单用户短期内重复相同请求过滤器
 * see https://mp.weixin.qq.com/s/D_srsQqCob729EGL0xsgIg
 */
@Component
@Slf4j
public class RequestDeDuplicator {

    @Resource(name = "auth-token-sentinel-string-redis-template")
    private StringRedisTemplate stringRedisTemplate;

    @Value("${request.duplicate.timeout}")
    private long expireTime;

    private static final String[] EXCLUDE_KEYS = new String[] {"date", "time"};

    public boolean isRequestDuplicate(final HttpServletRequest request, Authentication authentication) {

        String requestURI = request.getRequestURI();
        String userId = getUserIdFromRequest(authentication);
        if (StrUtil.isNotEmpty(userId)) {
            String requestParamMd5 = getRequestParamMd5(request);

            String key = "dedup:U=" + userId + "M=" + requestURI + "P=" + requestParamMd5;
            String val = "expireAt@" + System.currentTimeMillis() + expireTime;

            Boolean isFirstSet = stringRedisTemplate.execute(
                (RedisCallback<Boolean>)connection -> connection.set(key.getBytes(), val.getBytes(), Expiration.milliseconds(expireTime),
                    RedisStringCommands.SetOption.SET_IF_ABSENT));

            boolean isConsiderDuplicateRequest;
            isConsiderDuplicateRequest = isFirstSet == null || !isFirstSet;
            return isConsiderDuplicateRequest;
        }
        return true;
    }

    private String getUserIdFromRequest(Authentication authentication) {
        OAuth2Authentication auth2Authentication = (OAuth2Authentication)authentication;
        Object principal = auth2Authentication.getUserAuthentication().getPrincipal();
        if (principal instanceof SysUserDTO) {
            SysUserDTO user = (SysUserDTO)authentication.getPrincipal();
            return user.getIdStr();
        }
        return StrUtil.EMPTY;
    }

    private String getRequestParamMd5(final HttpServletRequest request) {
        return getRequestParamMd5(request, EXCLUDE_KEYS);
    }

    /**
     * 获取request的请求摘要
     *
     * @param excludeKeys 请求参数里面要去除哪些字段再求摘要
     * @return 去除部分忽略参数的MD5摘要
     */
    private String getRequestParamMd5(final HttpServletRequest request, String... excludeKeys) {

        JSONObject paramJson = (JSONObject)JSON.toJSON(request.getParameterMap());

        JSON bodyJson = null;
        try {
            if ("PUT".equals(request.getMethod()) || "POST".equals(request.getMethod())) {
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder responseStrBuilder = new StringBuilder();
                String s;
                while ((s = streamReader.readLine()) != null) {
                    responseStrBuilder.append(s);
                }
                String body = responseStrBuilder.toString();

                try {
                    bodyJson = JSONObject.parseObject(body);
                } catch (Exception ignored) {
                    try {
                        bodyJson = JSONObject.parseArray(body);
                    } catch (Exception e) {

                    }
                }
            }
        } catch (IOException | JSONException e) {
            log.error("Failed to get request body json from request {} cause ", request, e);
        }
        if (bodyJson == null) {
            bodyJson = new JSONObject();
        }

        if (bodyJson instanceof JSONObject) {
            paramJson.putAll((JSONObject)bodyJson);
        } else {
            JSONArray arr = (JSONArray)bodyJson;
            arr.forEach(obj -> {
                try {
                    JSONObject jsonObject = (JSONObject)obj;
                    paramJson.putAll(jsonObject);
                } catch (ClassCastException ignored) {
                    // TODO 当前端body传递的数据为形如["1","2","3"]的字符串数组时，此处类转换会发生不可预测的问题
                }
            });
        }

        if (paramJson.size() > 0) {
            @SuppressWarnings("unchecked") TreeMap<Object, Object> paramTreeMap = JSON.parseObject(paramJson.toJSONString(), TreeMap.class);
            if (excludeKeys != null) {
                List<String> deDuplicateExcludeKeys = Arrays.asList(excludeKeys);
                if (!deDuplicateExcludeKeys.isEmpty()) {
                    for (String excludeKey : deDuplicateExcludeKeys) {
                        paramTreeMap.remove(excludeKey);
                    }
                }
            }

            String paramTreeMapJSON = JSON.toJSONString(paramTreeMap);
            String md5deDupParam = jdkMD5(paramTreeMapJSON);
            log.debug("md5deDupParam = {}, excludeKeys = {}, paramJson = {}", md5deDupParam, Arrays.deepToString(excludeKeys), paramTreeMapJSON);

            return md5deDupParam;
        }
        return StrUtil.EMPTY;
    }

    private static String jdkMD5(String src) {
        String res = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] mdBytes = messageDigest.digest(src.getBytes());
            res = DatatypeConverter.printHexBinary(mdBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("" + e);
        }
        return res;
    }
}
