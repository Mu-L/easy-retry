package com.aizuda.easy.retry.server.job.task.support.request;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.easy.retry.client.model.request.DispatchJobResultRequest;
import com.aizuda.easy.retry.common.core.enums.HeadersEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.handler.PostHttpRequestHandler;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.callback.ClientCallbackContext;
import com.aizuda.easy.retry.server.job.task.support.callback.ClientCallbackFactory;
import com.aizuda.easy.retry.server.job.task.support.ClientCallbackHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.aizuda.easy.retry.common.core.constant.SystemConstants.HTTP_PATH.REPORT_JOB_DISPATCH_RESULT;

/**
 * @author www.byteblogs.com
 * @date 2023-09-30 23:01:58
 * @since 2.4.0
 */
@Slf4j
@Component
public class ReportDispatchResultPostHttpRequestHandler extends PostHttpRequestHandler {

    @Override
    public boolean supports(String path) {
        return REPORT_JOB_DISPATCH_RESULT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public String doHandler(String content, UrlQuery query, HttpHeaders headers) {
       EasyRetryLog.LOCAL.info("Client Callback Request. content:[{}]", content);

        EasyRetryRequest retryRequest = JsonUtil.parseObject(content, EasyRetryRequest.class);
        Object[] args = retryRequest.getArgs();

        DispatchJobResultRequest dispatchJobResultRequest = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), DispatchJobResultRequest.class);

        ClientCallbackHandler clientCallback = ClientCallbackFactory.getClientCallback(dispatchJobResultRequest.getTaskType());

        ClientCallbackContext context = JobTaskConverter.INSTANCE.toClientCallbackContext(dispatchJobResultRequest);
        context.setNamespaceId(headers.getAsString(HeadersEnum.NAMESPACE.getKey()));
        clientCallback.callback(context);

        return JsonUtil.toJsonString(new NettyResult(StatusEnum.YES.getStatus(), "Report Dispatch Result Processed Successfully", Boolean.TRUE, retryRequest.getReqId()));
    }
}
