package com.project.uptime_status.queue;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.project.uptime_status.service.TargetService;

@Component
public class ProbeQueue {
    
    private final StringRedisTemplate redisTemplate;
    private final TargetService targetService;
    private static final String QUEUE_KEY = "uptime:probe";

    public ProbeQueue(StringRedisTemplate redisTemplate, TargetService targetService) {
        
        this.redisTemplate = redisTemplate;
        this.targetService = targetService;
    }

    public void enqueue(String targetKey) {

        targetService.getTarget(targetKey);
        redisTemplate.opsForList().leftPush(QUEUE_KEY, targetKey);
    }

    public String dequeue() {

        return redisTemplate.opsForList().rightPop(QUEUE_KEY);
    }

}
