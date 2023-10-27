package com.aizuda.easy.retry.server.job.task.support.timer;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.job.task.support.idempotent.TimerIdempotent;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-22 17:03
 * @since : 2.4.0
 */
@Component
@Slf4j
public class JobTimerWheel implements Lifecycle {

    private static final int TICK_DURATION = 100;
    private static final String THREAD_NAME_PREFIX = "job-task-timer-wheel-";
    private static HashedWheelTimer timer = null;

    private static final TimerIdempotent idempotent = new TimerIdempotent();

    @Override
    public void start() {
        timer = new HashedWheelTimer(
                new CustomizableThreadFactory(THREAD_NAME_PREFIX), TICK_DURATION,
                TimeUnit.MILLISECONDS);
        timer.start();
    }

    public static void register(Long uniqueId, TimerTask task, long delay, TimeUnit unit) {

        if (!isExisted(uniqueId)) {
            delay = delay < 0 ? 0 : delay;
            log.info("加入时间轮. delay:[{}ms] uniqueId:[{}]", delay, uniqueId);
            try {
                timer.newTimeout(task, delay, unit);
                idempotent.set(uniqueId, uniqueId);
            } catch (Exception e) {
                LogUtils.error(log, "加入时间轮失败. uniqueId:[{}]", uniqueId, e);
            }
        }
    }

    public static boolean isExisted(Long uniqueId) {
        return idempotent.isExist(uniqueId, uniqueId);
    }

    public static void clearCache(Long uniqueId) {
        idempotent.clear(uniqueId, uniqueId);
    }

    @Override
    public void close() {
        timer.stop();
    }
}