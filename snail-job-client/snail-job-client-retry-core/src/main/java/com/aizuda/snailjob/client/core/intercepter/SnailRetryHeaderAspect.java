package com.aizuda.snailjob.client.core.intercepter;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.model.SnailJobHeaders;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * 服务间调用传递请求头和响应头
 *
 * @author: opensnail
 * @date : 2022-04-18 09:19
 * @since 1.0.0
 */
@Aspect
@Component
@Slf4j
public class SnailRetryHeaderAspect {

    public void before() {
        if (skip()) {
            return;
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(attributes)) {
            return;
        }

        String xRetry = attributes.getRequest().getHeader(SystemConstants.SNAIL_JOB_HEAD_KEY);
        if (Objects.nonNull(xRetry)) {
            // 标记进入方法的时间
            RetrySiteSnapshot.setEntryMethodTime(System.currentTimeMillis());

            SnailJobLog.LOCAL.info("snail retry request header :[{}]", xRetry);
            RetrySiteSnapshot.setRetryHeader(JsonUtil.parseObject(xRetry, SnailJobHeaders.class));
        }
    }

    @Around(value = "@within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        before();

        Throwable throwable = null;
        Object result = null;
        try {
            result = point.proceed();
        } catch (Throwable t) {
            throwable = t;
        } finally {
            afterReturning();
        }

        if (throwable != null) {
            throw throwable;
        } else {
            return result;
        }
    }

    public void afterReturning() {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(attributes)) {
            return;
        }

        HttpServletResponse response = attributes.getResponse();
        if (Objects.nonNull(response)) {
            response.addHeader(SystemConstants.SNAIL_JOB_STATUS_CODE_KEY, RetrySiteSnapshot.getRetryStatusCode());
        }

        if (skip()) {
            return;
        }

        // 服务端重试的在com.aizuda.snailjob.client.core.client.SnailRetryEndPoint.dispatch 中进行清除threadLocal
        if (Objects.nonNull(RetrySiteSnapshot.getStage()) && RetrySiteSnapshot.EnumStage.REMOTE.getStage() == RetrySiteSnapshot.getStage()) {
            return;
        }

        // 这里清除是为了,非服务端直接触发的节点，需要清除 threadLocal里面的标记
        RetrySiteSnapshot.removeRetryHeader();
        RetrySiteSnapshot.removeRetryStatusCode();
        RetrySiteSnapshot.removeEntryMethodTime();

    }

    /**
     * 本地重试不执行afterReturning和before方法，避免header传递失效
     *
     * @return
     */
    private boolean skip() {

        Integer stage = RetrySiteSnapshot.getStage();
        if (Objects.nonNull(stage) && RetrySiteSnapshot.EnumStage.LOCAL.getStage() == RetrySiteSnapshot.getStage()) {
            return true;
        }

        return false;
    }
}
