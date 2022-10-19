package com.cnasoft.health.userservice.platform;

import com.alibaba.fastjson.JSONObject;
import com.cnasoft.health.common.encryptor.DesensitizedUtil;
import com.cnasoft.health.common.enums.RoleEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class RemoteRestAPI {

    @Value("${third.resource-platform.url}")
    private String url;
    @Value("${third.resource-platform.servername}")
    private String servername;
    @Value("${third.resource-platform.server-password}")
    private String serverPassword;
    @Value("${third.resource-platform.key}")
    private String key;
    @Value("${third.resource-platform.iv}")
    private String iv;

    private final String HEADER_TOKEN = "Authorization";

    private final String tokenApi = "/api/public/token";
    private final String codeApi = "/api/public/code";
    private final String userApi = "/api/public/user";
    private final String evaluateApi = "/api/public/evaluate";
    private final String evaluateBatchApi = "/api/public/evaluate_batch";

    private String token = "";
    private Long expires = 0L;

    /**
     * 获取服务器认证token 缓存在内存中，通过expires判断是否失效
     *
     * @throws Exception
     */
    private void refreshTokenByApi() throws Exception {
        JSONObject jsonObject = new JSONObject();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url + tokenApi);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
        //httpPost.addHeader(HEADER_TOKEN, token);
        jsonObject.put("serverInfo", getServerInfo());
        httpPost.setEntity(paramEntity(jsonObject));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine == null || statusLine.getStatusCode() != HttpStatus.SC_OK) {
                int httpCode = statusLine == null ? -1 : statusLine.getStatusCode();
                log.error("获取资源平台的服务器TOKEN失败，http code {}", httpCode);
                throw new BadCredentialsException("获取资源平台的服务器TOKEN失败，http code " + httpCode);
            }
            HttpEntity entity = response.getEntity();
            String msg = EntityUtils.toString(entity);
            ObjectMapper mapper = new ObjectMapper();
            JSONObject result = mapper.readValue(msg, JSONObject.class);
            if (HttpStatus.SC_OK != result.getInteger("code")) {
                log.error("获取资源平台的服务器TOKEN失败:{}", result.getString("msg"));
                throw new BadCredentialsException("获取资源平台的服务器TOKEN失败:" + result.getString("msg"));
            } else {
                token = result.getJSONObject("data").getString("token");
                expires = System.currentTimeMillis() + result.getJSONObject("data").getLong("expires");
            }
        }
    }

    /**
     * 获取用户授权code 用于无感登录资源平台
     *
     * @param userInfo 用户信息
     * @return 临时校验码
     */
    public String getUserCode(UserInfo userInfo) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url + codeApi);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
        httpPost.addHeader(HEADER_TOKEN, getToken());
        handleUserInfo(userInfo);
        httpPost.setEntity(paramEntity(userInfo));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine == null || statusLine.getStatusCode() != HttpStatus.SC_OK) {
                int httpCode = statusLine == null ? -1 : statusLine.getStatusCode();
                log.error("获取资源平台的用户临时验证码失败，http code {}", httpCode);
                throw new BadCredentialsException("获取资源平台的用户临时验证码失败，http code " + httpCode);
            }

            HttpEntity entity = response.getEntity();
            String msg = EntityUtils.toString(entity);
            ObjectMapper mapper = new ObjectMapper();
            JSONObject result = mapper.readValue(msg, JSONObject.class);
            if (HttpStatus.SC_OK != result.getInteger("code")) {
                log.error("获取资源平台的用户临时验证码失败:{}", result.getString("msg"));
                throw new BadCredentialsException("获取资源平台的用户临时验证码失败:" + result.getString("msg"));
            } else {
                return result.getJSONObject("data").getString("code");
            }
        }
    }

    /**
     * 推送评测结果
     *
     * @param userInfo 用户信息
     * @return 临时校验码
     */
    public Integer pushUserInfo(UserInfo userInfo) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url + userApi);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
        httpPost.addHeader(HEADER_TOKEN, getToken());
        handleUserInfo(userInfo);
        httpPost.setEntity(paramEntity(userInfo));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine == null || statusLine.getStatusCode() != HttpStatus.SC_OK) {
                int httpCode = statusLine == null ? -1 : statusLine.getStatusCode();
                log.error("推送用户评测结果到资源平台失败，http code {}", httpCode);
                throw new BadCredentialsException("推送用户评测结果到资源平台失败，http code " + httpCode);
            }

            HttpEntity entity = response.getEntity();
            String msg = EntityUtils.toString(entity);
            ObjectMapper mapper = new ObjectMapper();
            JSONObject result = mapper.readValue(msg, JSONObject.class);
            if (HttpStatus.SC_OK != result.getInteger("code")) {
                log.error("推送用户评测结果到资源平台失败:{}", result.getString("msg"));
                throw new BadCredentialsException("推送用户评测结果到资源平台失败:" + result.getString("msg"));
            } else {
                return result.getJSONObject("data").getInteger("recommend");
            }
        }
    }

    /**
     * 推送量表评测静态数据
     *
     * @param evaluateInfo 评测数据
     */
    public void sendEvaluateResult(EvaluateInfo evaluateInfo) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url + evaluateApi);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
        httpPost.addHeader(HEADER_TOKEN, getToken());
        httpPost.setEntity(paramEntity(evaluateInfo));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine == null || statusLine.getStatusCode() != HttpStatus.SC_OK) {
                int httpCode = statusLine == null ? -1 : statusLine.getStatusCode();
                log.error("推送资源平台量表评测数据失败，http code {}", httpCode);
                throw new BadCredentialsException("推送资源平台量表评测数据失败，http code " + httpCode);
            }

            HttpEntity entity = response.getEntity();
            String msg = EntityUtils.toString(entity);
            ObjectMapper mapper = new ObjectMapper();
            JSONObject result = mapper.readValue(msg, JSONObject.class);
            if (HttpStatus.SC_OK != result.getInteger("code")) {
                log.error("推送资源平台量表评测数据失败:{}", result.getString("msg"));
                throw new BadCredentialsException("推送资源平台量表评测数据失败:" + result.getString("msg"));
            }
        }
    }

    /**
     * 批量推送量表评测静态数据
     *
     * @param evaluateInfo 评测数据
     */
    public void sendEvaluateBatchResult(List<EvaluateInfo> evaluateInfo) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url + evaluateBatchApi);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
        httpPost.addHeader(HEADER_TOKEN, getToken());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("evaluateList", evaluateInfo);
        httpPost.setEntity(paramEntity(jsonObject));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine == null || statusLine.getStatusCode() != HttpStatus.SC_OK) {
                int httpCode = statusLine == null ? -1 : statusLine.getStatusCode();
                log.error("批量推送资源平台量表评测数据失败，http code {}", httpCode);
                throw new BadCredentialsException("批量推送资源平台量表评测数据失败，http code " + httpCode);
            }

            HttpEntity entity = response.getEntity();
            String msg = EntityUtils.toString(entity);
            ObjectMapper mapper = new ObjectMapper();
            JSONObject result = mapper.readValue(msg, JSONObject.class);
            if (HttpStatus.SC_OK != result.getInteger("code")) {
                log.error("批量推送资源平台量表评测数据失败:{}", result.getString("msg"));
                throw new BadCredentialsException("批量推送资源平台量表评测数据失败:" + result.getString("msg"));
            }
        }
    }

    /**
     * 生成UID，加密姓名、身份证、手机号
     *
     * @param userInfo 用户信息
     */
    private void handleUserInfo(UserInfo userInfo) throws Exception {
        if (RoleEnum.student.getValue().equals(userInfo.getRoleCode())) {
            if (userInfo.getIdno() == null) {
                log.error("批量推送资源平台,用户信息异常，学生身份证为空，用户姓名:{}", userInfo.getName());
                throw new BadCredentialsException("批量推送资源平台,用户信息异常，用户姓名:" + userInfo.getName());
            }
            userInfo.setUid(getSHA1Srt(userInfo.getIdno()));
        } else {
            if (userInfo.getPhone() == null) {
                log.error("批量推送资源平台,用户信息异常，手机号为空，用户姓名:{}", userInfo.getName());
                throw new BadCredentialsException("批量推送资源平台,手机号为空，用户姓名:" + userInfo.getName());
            }
            userInfo.setUid(getSHA1Srt(userInfo.getPhone()));
        }
        // 脱敏 姓名、身份证、手机号
        userInfo.setName(DesensitizedUtil.desensitized(userInfo.getName(), DesensitizedUtil.DesensitizedType.CHINESE_NAME));
        userInfo.setPhone(DesensitizedUtil.desensitized(userInfo.getPhone(), DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
        userInfo.setIdno(DesensitizedUtil.desensitized(userInfo.getIdno(), DesensitizedUtil.DesensitizedType.ID_CARD));
    }

    private String getToken() throws Exception {
        Long time = System.currentTimeMillis();
        if (expires < time || StringUtils.isEmpty(token)) {
            refreshTokenByApi();
        }
        return token;
    }

    private String getServerInfo() throws Exception {
        return AESUtil.encryptAES(key, iv, servername + "|" + serverPassword);
    }

    private static HttpEntity paramEntity(Object reqBody) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("serialNo", UUID.randomUUID().toString().replace("-", ""));
        jsonObject.put("reqTime", new Date().getTime());
        jsonObject.put("reqBody", reqBody);
        log.info("推送数据到资源平台，请求体：" + jsonObject.toJSONString());
        return new StringEntity(jsonObject.toJSONString(), "utf-8");
    }

    private String getSHA1Srt(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(str.getBytes());
            byte[] digest = md.digest();

            StringBuilder hexStr = new StringBuilder();
            String shaHex;
            for (byte b : digest) {
                shaHex = Integer.toHexString(b & 0xFF);
                if (shaHex.length() < 2) {
                    hexStr.append(0);
                }
                hexStr.append(shaHex);
            }
            return hexStr.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
