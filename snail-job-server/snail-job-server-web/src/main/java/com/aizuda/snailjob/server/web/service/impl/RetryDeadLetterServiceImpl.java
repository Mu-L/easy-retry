package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;

import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.web.service.RetryDeadLetterService;
import com.aizuda.snailjob.server.web.service.convert.RetryDeadLetterResponseVOConverter;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.BatchDeleteRetryDeadLetterVO;
import com.aizuda.snailjob.server.web.model.request.BatchRollBackRetryDeadLetterVO;
import com.aizuda.snailjob.server.web.model.request.RetryDeadLetterQueryVO;
import com.aizuda.snailjob.server.web.model.response.RetryDeadLetterResponseVO;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.ConfigAccess;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: opensnail
 * @date : 2022-02-28 09:46
 */
@Service
public class RetryDeadLetterServiceImpl implements RetryDeadLetterService {

    @Autowired
    private AccessTemplate accessTemplate;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;

    @Override
    public PageResult<List<RetryDeadLetterResponseVO>> getRetryDeadLetterPage(RetryDeadLetterQueryVO queryVO) {

        PageDTO<RetryDeadLetter> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        if (StrUtil.isBlank(queryVO.getGroupName())) {
            return new PageResult<>(pageDTO, new ArrayList<>());
        }

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        LambdaQueryWrapper<RetryDeadLetter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RetryDeadLetter::getNamespaceId, namespaceId);
        queryWrapper.eq(RetryDeadLetter::getGroupName, queryVO.getGroupName());

        if (StrUtil.isNotBlank(queryVO.getSceneName())) {
            queryWrapper.eq(RetryDeadLetter::getSceneName, queryVO.getSceneName());
        }

        if (StrUtil.isNotBlank(queryVO.getBizNo())) {
            queryWrapper.eq(RetryDeadLetter::getBizNo, queryVO.getBizNo());
        }

        if (StrUtil.isNotBlank(queryVO.getIdempotentId())) {
            queryWrapper.eq(RetryDeadLetter::getIdempotentId, queryVO.getIdempotentId());
        }

        if (StrUtil.isNotBlank(queryVO.getUniqueId())) {
            queryWrapper.eq(RetryDeadLetter::getUniqueId, queryVO.getUniqueId());
        }

        PageDTO<RetryDeadLetter> retryDeadLetterPageDTO = accessTemplate.getRetryDeadLetterAccess()
                .listPage(queryVO.getGroupName(), namespaceId, pageDTO, queryWrapper);

        return new PageResult<>(retryDeadLetterPageDTO,
                RetryDeadLetterResponseVOConverter.INSTANCE.batchConvert(retryDeadLetterPageDTO.getRecords()));
    }

    @Override
    public RetryDeadLetterResponseVO getRetryDeadLetterById(String groupName, Long id) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        RetryDeadLetter retryDeadLetter = retryDeadLetterAccess.one(groupName, namespaceId,
                new LambdaQueryWrapper<RetryDeadLetter>().eq(RetryDeadLetter::getId, id));
        return RetryDeadLetterResponseVOConverter.INSTANCE.convert(retryDeadLetter);
    }

    @Override
    @Transactional
    public int rollback(BatchRollBackRetryDeadLetterVO rollBackRetryDeadLetterVO) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        String groupName = rollBackRetryDeadLetterVO.getGroupName();
        List<Long> ids = rollBackRetryDeadLetterVO.getIds();
        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        List<RetryDeadLetter> retryDeadLetterList = retryDeadLetterAccess.list(groupName, namespaceId,
                new LambdaQueryWrapper<RetryDeadLetter>().in(RetryDeadLetter::getId, ids));

        Assert.notEmpty(retryDeadLetterList, () -> new SnailJobServerException("数据不存在"));

        ConfigAccess<RetrySceneConfig> sceneConfigAccess = accessTemplate.getSceneConfigAccess();
        Set<String> sceneNameSet = retryDeadLetterList.stream().map(RetryDeadLetter::getSceneName)
                .collect(Collectors.toSet());
        List<RetrySceneConfig> retrySceneConfigs = sceneConfigAccess.list(new LambdaQueryWrapper<RetrySceneConfig>()
                .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                .in(RetrySceneConfig::getSceneName, sceneNameSet));

        Map<String, RetrySceneConfig> sceneConfigMap = retrySceneConfigs.stream().collect(Collectors.toMap((sceneConfig) ->
                sceneConfig.getGroupName() + sceneConfig.getSceneName(), Function.identity()));

        List<RetryTask> waitRollbackList = new ArrayList<>();
        for (RetryDeadLetter retryDeadLetter : retryDeadLetterList) {
            RetrySceneConfig retrySceneConfig = sceneConfigMap.get(
                    retryDeadLetter.getGroupName() + retryDeadLetter.getSceneName());
            Assert.notNull(retrySceneConfig,
                    () -> new SnailJobServerException("未查询到场景. [{}]", retryDeadLetter.getSceneName()));

            RetryTask retryTask = RetryTaskConverter.INSTANCE.toRetryTask(retryDeadLetter);
            retryTask.setRetryStatus(RetryStatusEnum.RUNNING.getStatus());
            retryTask.setTaskType(SyetemTaskTypeEnum.RETRY.getType());

            WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
            waitStrategyContext.setNextTriggerAt(LocalDateTime.now());
            waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
            waitStrategyContext.setDelayLevel(1);
            WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff());
            retryTask.setNextTriggerAt(DateUtils.toLocalDateTime(waitStrategy.computeTriggerTime(waitStrategyContext)));
            retryTask.setCreateDt(LocalDateTime.now());
            waitRollbackList.add(retryTask);
        }

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        Assert.isTrue(waitRollbackList.size() == retryTaskAccess.batchInsert(groupName, namespaceId, waitRollbackList),
                () -> new SnailJobServerException("新增重试任务失败"));

        Set<Long> waitDelRetryDeadLetterIdSet = retryDeadLetterList.stream().map(RetryDeadLetter::getId)
                .collect(Collectors.toSet());
        Assert.isTrue(waitDelRetryDeadLetterIdSet.size() == retryDeadLetterAccess.delete(groupName, namespaceId,
                        new LambdaQueryWrapper<RetryDeadLetter>()
                                .eq(RetryDeadLetter::getGroupName, groupName)
                                .in(RetryDeadLetter::getId, waitDelRetryDeadLetterIdSet)),
                () -> new SnailJobServerException("删除死信队列数据失败"))
        ;

        // 变更日志的状态
        RetryTaskLog retryTaskLog = new RetryTaskLog();
        retryTaskLog.setRetryStatus(RetryStatusEnum.RUNNING.getStatus());

        Set<String> uniqueIdSet = waitRollbackList.stream().map(RetryTask::getUniqueId).collect(Collectors.toSet());
        int update = retryTaskLogMapper.update(retryTaskLog, new LambdaUpdateWrapper<RetryTaskLog>()
                .eq(RetryTaskLog::getNamespaceId, namespaceId)
                .in(RetryTaskLog::getUniqueId, uniqueIdSet)
                .eq(RetryTaskLog::getGroupName, groupName));
        Assert.isTrue(update == uniqueIdSet.size(),
                () -> new SnailJobServerException("回滚日志状态失败, 可能原因: 日志信息缺失或存在多个相同uniqueId"));

        return update;
    }

    @Override
    public int batchDelete(BatchDeleteRetryDeadLetterVO deadLetterVO) {
        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        return retryDeadLetterAccess.delete(deadLetterVO.getGroupName(), namespaceId,
                new LambdaQueryWrapper<RetryDeadLetter>()
                        .eq(RetryDeadLetter::getNamespaceId, namespaceId)
                        .eq(RetryDeadLetter::getGroupName, deadLetterVO.getGroupName())
                        .in(RetryDeadLetter::getId, deadLetterVO.getIds()));
    }
}
