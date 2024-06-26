package com.aizuda.snailjob.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: xiaowoniu
 * @date : 2023-12-15 12:22
 * @since : 2.6.0
 */
@Data
public class WorkflowResponseVO {

    private Long id;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 触发类型
     */
    private Integer triggerType;

    /**
     * 触发间隔
     */
    private String triggerInterval;

    /**
     * 执行超时时间
     */
    private Integer executorTimeout;

    /**
     * 工作流状态 0、关闭、1、开启
     */
    private Integer workflowStatus;

    /**
     * 任务执行时间
     */
    private LocalDateTime nextTriggerAt;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 修改时间
     */
    private LocalDateTime updateDt;

}
