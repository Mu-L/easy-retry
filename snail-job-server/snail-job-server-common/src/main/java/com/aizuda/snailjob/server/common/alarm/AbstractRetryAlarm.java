package com.aizuda.snailjob.server.common.alarm;

import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo;
import com.aizuda.snailjob.server.common.dto.RetryAlarmInfo;
import com.aizuda.snailjob.server.common.enums.SystemModeEnum;
import com.aizuda.snailjob.server.common.triple.ImmutableTriple;
import com.aizuda.snailjob.server.common.triple.Triple;
import org.springframework.context.ApplicationEvent;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:19:19
 * @since 2.5.0
 */
public abstract class AbstractRetryAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, RetryAlarmInfo> {
    @Override
    protected Map<Triple<String, String, String>, List<RetryAlarmInfo>> convertAlarmDTO(
            List<RetryAlarmInfo> alarmDataList,
            Set<String> namespaceIds,
            Set<String> groupNames,
            Set<String> sceneNames) {

        return alarmDataList.stream()
                .collect(Collectors.groupingBy(i -> {

                    String namespaceId = i.getNamespaceId();
                    String groupName = i.getGroupName();
                    String sceneName = i.getSceneName();

                    namespaceIds.add(namespaceId);
                    groupNames.add(groupName);
                    sceneNames.add(sceneName);

                    return ImmutableTriple.of(namespaceId, groupName, sceneName);
                }));
    }

}
