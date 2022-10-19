package com.cnasoft.health.auth.service.impl;

import com.cnasoft.health.auth.constant.RedisDataSource;
import com.cnasoft.health.auth.details.DefaultClientDetails;
import com.cnasoft.health.common.constant.SecurityConstants;
import com.cnasoft.health.common.redis.RedisRepository;
import com.cnasoft.health.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * oauth_client_details表缓存优化
 *
 * @author cnasoft
 * @date 2020/7/2 19:41
 */
@Slf4j
@Service
public class RedisClientDetailsService extends JdbcClientDetailsService {
    /**
     * 扩展默认的 ClientDetailsService, 增加逻辑删除判断( is_deleted  = 0)
     */
    private static final String SELECT_CLIENT_DETAILS_SQL =
        "select client_id, client_secret, resource_ids, scope, authorized_grant_types, " + "web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, "
            + "additional_information, autoapprove ,limit_flag, limit_count " + "from oauth_client_details where client_id = ? and is_deleted = 0 ";

    /**
     * 执行逻辑删除(is_deleted = 1)
     */
    private static final String DEFAULT_DELETE_STATEMENT_SQL = "update oauth_client_details set is_deleted = 1 where client_id = ?";

    private RedisRepository redisRepository;

    private final JdbcTemplate jdbcTemplate;

    public RedisClientDetailsService(DataSource dataSource, @Qualifier(RedisDataSource.AUTH_TOKEN_SOURCE) RedisRepository redisRepository) {
        super(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        setSelectClientDetailsSql(SELECT_CLIENT_DETAILS_SQL);
        setDeleteClientDetailsSql(DEFAULT_DELETE_STATEMENT_SQL);
        this.redisRepository = redisRepository;
        cacheAndGetClient("app");
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) {
        // 先从redis获取
        ClientDetails clientDetails = null;
        String clientJson = redisRepository.get(clientRedisKey(clientId));

        if (StringUtils.isNotBlank(clientJson)) {
            clientDetails = JsonUtils.readValue(clientJson, BaseClientDetails.class);
        }

        if (clientDetails == null) {
            clientDetails = cacheAndGetClient(clientId);
        }
        return clientDetails;
    }

    /**
     * 缓存client并返回client
     *
     * @param clientId
     * @return
     */
    private ClientDetails cacheAndGetClient(String clientId) {
        // 从数据库读取
        ClientDetails clientDetails = null;
        try {
            clientDetails = jdbcTemplate.queryForObject(SELECT_CLIENT_DETAILS_SQL, new ClientDetailsRowMapper(), clientId);
            if (clientDetails != null) {
                // 写入redis缓存
                redisRepository.set(clientRedisKey(clientId), JsonUtils.writeValueAsString(clientDetails));
                log.info("cache clientId:{},{}", clientId, clientDetails);
            }
        } catch (NoSuchClientException e) {
            log.error("clientId:{},{}", clientId, clientId);
        } catch (InvalidClientException e) {
            log.error("cacheAndGetClient-invalidClient:{}", clientId, e);
        }
        return clientDetails;
    }

    /**
     * 新增限流标识映射, 供网关限流使用.
     */
    private class ClientDetailsRowMapper implements RowMapper<ClientDetails> {
        @Override
        public ClientDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
            DefaultClientDetails details = new DefaultClientDetails(rs.getString(1), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(7), rs.getString(6));
            details.setClientSecret(rs.getString(2));
            if (rs.getObject(8) != null) {
                details.setAccessTokenValiditySeconds(rs.getInt(8));
            }
            if (rs.getObject(9) != null) {
                details.setRefreshTokenValiditySeconds(rs.getInt(9));
            }
            String json = rs.getString(10);
            if (StringUtils.isNotBlank(json)) {
                Map<String, Object> additionalInformation = JsonUtils.readValue(json, Map.class);
                details.setAdditionalInformation(additionalInformation);
            }

            String scopes = rs.getString(11);
            details.setLimitFlag(rs.getInt(12));
            details.setLimitCount(rs.getInt(13));
            if (scopes != null) {
                details.setAutoApproveScopes(org.springframework.util.StringUtils.commaDelimitedListToSet(scopes));
            }
            return details;
        }
    }

    @Override
    public void updateClientDetails(ClientDetails clientDetails) {
        super.updateClientDetails(clientDetails);
        cacheAndGetClient(clientDetails.getClientId());
    }

    @Override
    public void updateClientSecret(String clientId, String secret) {
        super.updateClientSecret(clientId, secret);
        cacheAndGetClient(clientId);
    }

    @Override
    public void removeClientDetails(String clientId) {
        super.removeClientDetails(clientId);
        removeRedisCache(clientId);
    }

    /**
     * 删除redis缓存
     *
     * @param clientId 客户端ID
     */
    private void removeRedisCache(String clientId) {
        redisRepository.del(clientRedisKey(clientId));
    }

    private String clientRedisKey(String clientId) {
        return SecurityConstants.CACHE_CLIENT_KEY + ":" + clientId;
    }
}
