package com.aizuda.snailjob.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: opensnail
 * @date : 2022-03-03 10:56
 */
@Data
public class SceneConfigResponseVO {

    private Long id;

    private String groupName;

    private String sceneName;

    private Integer sceneStatus;

    private Integer maxRetryCount;

    private Integer backOff;

    private String triggerInterval;

    private String description;

    private Long deadlineRequest;

    private Integer routeKey;

    private Integer executorTimeout;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

}
