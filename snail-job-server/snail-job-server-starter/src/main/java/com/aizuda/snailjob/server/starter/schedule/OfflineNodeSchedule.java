package com.aizuda.snailjob.server.starter.schedule;

import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.register.ServerRegister;
import com.aizuda.snailjob.server.common.schedule.AbstractSchedule;
import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 删除过期下线机器
 *
 * @author: opensnail
 * @date : 2023-07-21 14:59
 * @since 2.1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OfflineNodeSchedule extends AbstractSchedule implements Lifecycle {
    private final ServerNodeMapper serverNodeMapper;

    @Override
    protected void doExecute() {

        try {
            // 删除内存缓存的待下线的机器
            LocalDateTime endTime = LocalDateTime.now().minusSeconds(
                    ServerRegister.DELAY_TIME + (ServerRegister.DELAY_TIME / 3));

            // 先删除DB中需要下线的机器
            serverNodeMapper.delete(new LambdaQueryWrapper<ServerNode>().le(ServerNode::getExpireAt, endTime));

            Set<RegisterNodeInfo> allPods = CacheRegisterTable.getAllPods();
            Set<RegisterNodeInfo> waitOffline = allPods.stream().filter(registerNodeInfo -> registerNodeInfo.getExpireAt().isBefore(endTime)).collect(
                    Collectors.toSet());
            Set<String> podIds = StreamUtils.toSet(waitOffline, RegisterNodeInfo::getHostId);
            if (CollectionUtils.isEmpty(podIds)) {
                return;
            }

            for (final RegisterNodeInfo registerNodeInfo : waitOffline) {
                CacheRegisterTable.remove(registerNodeInfo.getGroupName(), registerNodeInfo.getNamespaceId(), registerNodeInfo.getHostId());
            }

        } catch (Exception e) {
            SnailJobLog.LOCAL.error("clearOfflineNode 失败", e);
        }
    }

    @Override
    public String lockName() {
        return "clearOfflineNode";
    }

    @Override
    public String lockAtMost() {
        return "PT10S";
    }

    @Override
    public String lockAtLeast() {
        return "PT5S";
    }

    @Override
    public void start() {
        taskScheduler.scheduleWithFixedDelay(this::execute, Instant.now(), Duration.parse("PT5S"));
    }

    @Override
    public void close() {

    }
}
