package com.project.uptime_status.queue;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.project.uptime_status.service.TargetService;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class ProbeQueue {
    
    private final StringRedisTemplate redisTemplate;
    private final TargetService targetService;
    private static final String QUEUE_KEY = "uptime:probe";

    public ProbeQueue(StringRedisTemplate redisTemplate, TargetService targetService, MeterRegistry meterRegistry) {
        
        this.redisTemplate = redisTemplate;
        this.targetService = targetService;

        Gauge.builder("uptime.probe.queue.size", this, ProbeQueue::getQueueSize)
             .description("The current number of elements in the uptime probe queue")
             .register(meterRegistry);
    }

    public void enqueue(String targetKey) {

        targetService.getTarget(targetKey);
        redisTemplate.opsForList().leftPush(QUEUE_KEY, targetKey);
    }

    public String dequeue() {

        return redisTemplate.opsForList().rightPop(QUEUE_KEY);
    }

    private double getQueueSize() {
        try {
            Long size = redisTemplate.opsForList().size(QUEUE_KEY);
            return size != null ? size.doubleValue() : 0.0;
        } catch (Exception e) {
            return Double.NaN;
        }
    }

}
