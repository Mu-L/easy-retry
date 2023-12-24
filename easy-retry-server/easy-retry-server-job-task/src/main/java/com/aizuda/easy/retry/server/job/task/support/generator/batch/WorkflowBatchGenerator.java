package com.aizuda.easy.retry.server.job.task.support.generator.batch;

import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowTimerTaskDTO;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.timer.JobTimerTask;
import com.aizuda.easy.retry.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.easy.retry.server.job.task.support.timer.WorkflowTimerTask;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowTaskBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * @author: xiaowoniu
 * @date : 2023-12-22 09:04
 * @since : 2.6.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowBatchGenerator {
    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;
    @Transactional
    public void generateJobTaskBatch(WorkflowTaskBatchGeneratorContext context) {

        // 生成任务批次
        WorkflowTaskBatch workflowTaskBatch = WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatch(context);
        workflowTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.WAITING.getStatus());
        workflowTaskBatchMapper.insert(workflowTaskBatch);

        // 开始执行工作流
        // 进入时间轮
        long delay = context.getNextTriggerAt() - DateUtils.toNowMilli();
        WorkflowTimerTaskDTO workflowTimerTaskDTO = new WorkflowTimerTaskDTO();
        workflowTimerTaskDTO.setWorkflowTaskBatchId(workflowTaskBatch.getId());
        workflowTimerTaskDTO.setWorkflowId(context.getWorkflowId());
        workflowTimerTaskDTO.setTriggerType(context.getTriggerType());
        JobTimerWheel.register(workflowTaskBatch.getId(),
            new WorkflowTimerTask(workflowTimerTaskDTO), delay, TimeUnit.MILLISECONDS);
    }
}