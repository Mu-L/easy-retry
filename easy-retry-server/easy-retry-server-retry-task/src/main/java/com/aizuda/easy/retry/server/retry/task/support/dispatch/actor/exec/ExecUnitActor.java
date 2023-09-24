package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.exec;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.client.model.DispatchRetryDTO;
import com.aizuda.easy.retry.client.model.DispatchRetryResultDTO;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.RetryResultStatusEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.EasyRetryHeaders;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.client.RequestBuilder;
import com.aizuda.easy.retry.server.common.client.RpcClient;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.retry.task.support.IdempotentStrategy;
import com.aizuda.easy.retry.server.retry.task.support.context.MaxAttemptsPersistenceRetryContext;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.log.RetryTaskLogDTO;
import com.aizuda.easy.retry.server.retry.task.support.retry.RetryExecutor;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 重试结果执行器
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 2.0
 */
@Component(ActorGenerator.EXEC_UNIT_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ExecUnitActor extends AbstractActor  {

    @Autowired
    @Qualifier("bitSetIdempotentStrategyHandler")
    private IdempotentStrategy<String, Integer> idempotentStrategy;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryExecutor.class, retryExecutor -> {

            MaxAttemptsPersistenceRetryContext context = (MaxAttemptsPersistenceRetryContext) retryExecutor.getRetryContext();
            RetryTask retryTask = context.getRetryTask();
            RegisterNodeInfo serverNode = context.getServerNode();

            RetryTaskLogDTO retryTaskLog = new RetryTaskLogDTO();
            retryTaskLog.setGroupName(retryTask.getGroupName());
            retryTaskLog.setUniqueId(retryTask.getUniqueId());
            retryTaskLog.setRetryStatus(retryTask.getRetryStatus());

            try {

                if (Objects.nonNull(serverNode)) {

                    retryExecutor.call((Callable<Result<DispatchRetryResultDTO>>) () -> {

                        Result<DispatchRetryResultDTO> result = callClient(retryTask, serverNode);

                        // 回调接口请求成功，处理返回值
                        if (StatusEnum.YES.getStatus() != result.getStatus()) {
                            if (StrUtil.isNotBlank(result.getMessage())) {
                                retryTaskLog.setMessage(result.getMessage());
                            } else {
                                retryTaskLog.setMessage("客户端执行失败: 异常信息为空");
                            }
                        } else {
                            DispatchRetryResultDTO data = JsonUtil.parseObject(JsonUtil.toJsonString(result.getData()), DispatchRetryResultDTO.class);
                            result.setData(data);
                            if (Objects.nonNull(data)) {
                                if (RetryResultStatusEnum.FAILURE.getStatus().equals(data.getStatusCode())) {
                                    if (StrUtil.isNotBlank(data.getExceptionMsg())) {
                                        retryTaskLog.setMessage(data.getExceptionMsg());
                                    } else {
                                        retryTaskLog.setMessage("客户端重试失败: 异常信息为空");
                                    }
                                } else if (RetryResultStatusEnum.STOP.getStatus().equals(data.getStatusCode())) {
                                    retryTaskLog.setMessage("客户端主动停止任务");
                                } else {
                                    retryTaskLog.setMessage("客户端执行成功");
                                }
                            }

                        }

                        return result;
                    });

                    // 请求发生异常
                    if (context.hasException()) {
                        retryTaskLog.setMessage(context.getException().getMessage());
                    }
                } else {
                    retryTaskLog.setMessage("There are currently no available client PODs.");
                }

            }catch (Exception e) {
                LogUtils.error(log, "callback client error. retryTask:[{}]", JsonUtil.toJsonString(retryTask), e);
                retryTaskLog.setMessage(e.getMessage());
            } finally {

                // 清除幂等标识位
                idempotentStrategy.clear(retryTask.getGroupName(), retryTask.getId().intValue());
                ActorRef actorRef = ActorGenerator.logActor();
                actorRef.tell(retryTaskLog, actorRef);
                getContext().stop(getSelf());

            }

        }).build();
    }

    /**
     * 调用客户端
     *
     * @param retryTask {@link RetryTask} 需要重试的数据
     * @return 重试结果返回值
     */
    private Result<DispatchRetryResultDTO> callClient(RetryTask retryTask, RegisterNodeInfo serverNode) {

        DispatchRetryDTO dispatchRetryDTO = new DispatchRetryDTO();
        dispatchRetryDTO.setIdempotentId(retryTask.getIdempotentId());
        dispatchRetryDTO.setScene(retryTask.getSceneName());
        dispatchRetryDTO.setExecutorName(retryTask.getExecutorName());
        dispatchRetryDTO.setArgsStr(retryTask.getArgsStr());
        dispatchRetryDTO.setUniqueId(retryTask.getUniqueId());
        dispatchRetryDTO.setRetryCount(retryTask.getRetryCount());

        // 设置header
        HttpHeaders requestHeaders = new HttpHeaders();
        EasyRetryHeaders easyRetryHeaders = new EasyRetryHeaders();
        easyRetryHeaders.setEasyRetry(Boolean.TRUE);
        easyRetryHeaders.setEasyRetryId(retryTask.getUniqueId());
        requestHeaders.add(SystemConstants.EASY_RETRY_HEAD_KEY, JsonUtil.toJsonString(easyRetryHeaders));

        RpcClient rpcClient = RequestBuilder.<RpcClient, Result>newBuilder()
            .hostPort(serverNode.getHostPort())
            .groupName(serverNode.getGroupName())
            .hostId(serverNode.getHostId())
            .hostIp(serverNode.getHostIp())
            .contextPath(serverNode.getContextPath())
            .client(RpcClient.class)
            .build();

        return rpcClient.dispatch(dispatchRetryDTO, easyRetryHeaders);
    }


}