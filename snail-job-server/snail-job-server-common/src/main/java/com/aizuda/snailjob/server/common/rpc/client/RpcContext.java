package com.aizuda.snailjob.server.common.rpc.client;

import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * 处理请求服务端时需要存储的中间值
 *
 * @author: opensnail
 * @date : 2023-05-12 09:05
 * @since 1.3.0
 */
@Slf4j
public final class RpcContext {
    private RpcContext() {}

    private static final ConcurrentMap<Long, CompletableFuture> COMPLETABLE_FUTURE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Long, Consumer> CALLBACK_CONSUMER = new ConcurrentHashMap<>();

    public static void invoke(Long requestId, NettyResult nettyResult) {

        try {
            // 同步请求同步返回
            Optional.ofNullable(getCompletableFuture(requestId)).ifPresent(completableFuture -> completableFuture.complete(nettyResult));

            // 异步请求进行回调
            Optional.ofNullable(getConsumer(requestId)).ifPresent(consumer -> consumer.accept(nettyResult));
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("回调处理失败 requestId:[{}]",requestId, e );
        } finally {
            remove(requestId);
        }

    }

    public static void remove(Long requestId) {
        COMPLETABLE_FUTURE.remove(requestId);
        CALLBACK_CONSUMER.remove(requestId);
    }

    public static <R> void setCompletableFuture(long id, CompletableFuture<R> completableFuture, Consumer<R> callable) {
        if (Objects.nonNull(completableFuture)) {
            COMPLETABLE_FUTURE.put(id, completableFuture);
        }

        if (Objects.nonNull(callable)) {
            CALLBACK_CONSUMER.put(id, callable);
        }

    }

    public static <R> void setCompletableFuture(Long id, Consumer<R> callable) {
        setCompletableFuture(id, null, callable);
    }

    public static <R> void setCompletableFuture(Long id, CompletableFuture<R> completableFuture) {
        setCompletableFuture(id, completableFuture, null);
    }

    public static CompletableFuture<Object> getCompletableFuture(Long id) {
        return COMPLETABLE_FUTURE.get(id);
    }

    public static Consumer getConsumer(Long id) {
        return CALLBACK_CONSUMER.get(id);
    }
}
