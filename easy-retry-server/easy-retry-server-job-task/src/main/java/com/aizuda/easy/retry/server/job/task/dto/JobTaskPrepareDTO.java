package com.aizuda.easy.retry.server.job.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author www.byteblogs.com
 * @date 2023-09-25 22:42:21
 * @since 2.4.0
 */
@Data
public class JobTaskPrepareDTO {

    private Long jobId;

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
     * 任务类型
     */
    private Integer taskType;

    /**
     * 任务执行超时时间，单位秒
     */
    private Integer executorTimeout;

    private Long taskBatchId;

    private String clientId;

    /**
     * 任务执行时间
     */
    private LocalDateTime executionAt;

    private boolean onlyTimeoutCheck;

}
