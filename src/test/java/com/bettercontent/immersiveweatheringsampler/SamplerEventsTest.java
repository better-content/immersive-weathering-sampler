package com.bettercontent.immersiveweatheringsampler;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SamplerEventsTest {
    @Test void chunkIoThreadsShareOneConcurrentDimensionQueue() throws Exception {
        Map<String, Set<Long>> queues = new ConcurrentHashMap<>();
        int workers = 16;
        int additions = 1_000;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        try {
            for (int worker = 0; worker < workers; worker++) {
                final int offset = worker * additions;
                executor.submit(() -> {
                    start.await();
                    Set<Long> queue = SamplerEvents.queue(queues, "overworld");
                    for (int index = 0; index < additions; index++) queue.add((long) offset + index);
                    return null;
                });
            }
            start.countDown();
        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }

        assertEquals(Set.of("overworld"), queues.keySet());
        assertEquals(workers * additions, queues.get("overworld").size());
    }

    @Test void chunkIoThreadsShareOneConcurrentExposureIndex() throws Exception {
        Map<String, Map<Long, Integer>> dimensions = new ConcurrentHashMap<>();
        int workers = 16;
        int additions = 1_000;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        try {
            for (int worker = 0; worker < workers; worker++) {
                final int offset = worker * additions;
                executor.submit(() -> {
                    start.await();
                    Map<Long, Integer> entries = ChunkExposureData.entriesFor(dimensions, "overworld");
                    for (int index = 0; index < additions; index++) entries.put((long) offset + index, index);
                    return null;
                });
            }
            start.countDown();
        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }

        assertEquals(Set.of("overworld"), dimensions.keySet());
        assertEquals(workers * additions, dimensions.get("overworld").size());
    }
}
