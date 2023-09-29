package com.aizuda.easy.retry.server.job.task.dto;

import com.aizuda.easy.retry.server.common.dto.PartitionTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author:  www.byteblogs.com
 * @date : 2023-10-10 17:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobPartitionTask extends PartitionTask {

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 名称
     */
    private String jobName;

    /**
     * 下次触发时间
     */
    private LocalDateTime nextTriggerAt;

    /**
     * 阻塞策略 1、丢弃 2、覆盖 3、并行
     */
    private Integer blockStrategy;

    /**
     * 触发类型 1.CRON 表达式 2. 固定时间
     */
    private Integer triggerType;

    /**
     * 间隔时长
     */
    private String triggerInterval;

    /**
     * 任务执行超时时间，单位秒
     */
    private Integer executorTimeout;

}
