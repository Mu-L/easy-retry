package com.aizuda.snailjob.server.job.task.support.prepare.workflow;

import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTimerTaskDTO;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.snailjob.server.job.task.support.timer.WorkflowTimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 处理处于{@link JobTaskBatchStatusEnum::WAIT}状态的任务
 *
 * @author xiaowoniu
 * @date 2023-10-05 18:29:22
 * @since 2.6.0
 */
@Component
@Slf4j
public class WaiWorkflowPrepareHandler extends AbstractWorkflowPrePareHandler {

    @Override
    public boolean matches(Integer status) {
        return Objects.nonNull(status) && JobTaskBatchStatusEnum.WAITING.getStatus() == status;
    }

    @Override
    protected void doHandler(WorkflowTaskPrepareDTO workflowTaskPrepareDTO) {
        log.debug("存在待处理任务. workflowTaskBatchId:[{}]", workflowTaskPrepareDTO.getWorkflowTaskBatchId());

        // 若时间轮中数据不存在则重新加入
        if (!JobTimerWheel.isExisted(SyetemTaskTypeEnum.WORKFLOW.getType(), workflowTaskPrepareDTO.getWorkflowTaskBatchId())) {
            log.info("存在待处理任务且时间轮中不存在 workflowTaskBatchId:[{}]", workflowTaskPrepareDTO.getWorkflowTaskBatchId());

            // 进入时间轮
            long delay = workflowTaskPrepareDTO.getNextTriggerAt() - DateUtils.toNowMilli();
            WorkflowTimerTaskDTO workflowTimerTaskDTO = new WorkflowTimerTaskDTO();
            workflowTimerTaskDTO.setWorkflowTaskBatchId(workflowTaskPrepareDTO.getWorkflowTaskBatchId());
            workflowTimerTaskDTO.setWorkflowId(workflowTaskPrepareDTO.getWorkflowId());
            workflowTimerTaskDTO.setTaskExecutorScene(workflowTaskPrepareDTO.getTaskExecutorScene());
            JobTimerWheel.register(SyetemTaskTypeEnum.WORKFLOW.getType(), workflowTaskPrepareDTO.getWorkflowTaskBatchId(),
                    new WorkflowTimerTask(workflowTimerTaskDTO), delay, TimeUnit.MILLISECONDS);
        }
    }
}
