package com.cnasoft.health.auth.filter;

import com.cnasoft.health.common.exception.ServiceException;
import com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants;
import com.cnasoft.health.common.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Spring Security 异常处理
 *
 * @author cnasoft
 * @date 2020/8/15 11:27
 */
@Slf4j
@Component
public class FilterChainExceptionHandler extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ServiceException se) {
            ResponseUtil.responseFailed(response, se);
        } catch (Exception e) {
            log.error("[doFilterInternal] Spring Security Filter Chain Exception:", e);
            ResponseUtil.responseWriter(response, GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getMessage(), GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode());
        }
    }
}
