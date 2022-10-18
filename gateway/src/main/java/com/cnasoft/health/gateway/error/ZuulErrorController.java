package com.cnasoft.health.gateway.error;

import com.cnasoft.health.common.vo.CommonResult;
import com.netflix.zuul.context.RequestContext;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author cnasoft
 * @date 2020/6/8 19:32
 */
@Controller
public class ZuulErrorController implements ErrorController {

    public static final String ERROR_PATH = "/error";

    @RequestMapping(ERROR_PATH)
    @ResponseBody
    public Object error() {
        RequestContext ctx = RequestContext.getCurrentContext();
        Throwable throwable = ctx.getThrowable();
        return CommonResult.error(throwable.getMessage());
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}
