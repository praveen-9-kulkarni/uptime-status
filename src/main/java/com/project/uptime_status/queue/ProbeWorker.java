package com.project.uptime_status.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.uptime_status.service.TargetService;

@Component
@Profile("worker")
public class ProbeWorker {

    private final Logger log = LoggerFactory.getLogger(ProbeWorker.class);
    private final ProbeQueue probeQueue;
    private final TargetService targetService;

    public ProbeWorker(ProbeQueue probeQueue, TargetService targetService) {

        this.probeQueue = probeQueue;
        this.targetService = targetService;
    }

    @Scheduled(fixedDelay = 1000)
    public void work() {

        String targetKey = probeQueue.dequeue();
        if (targetKey == null) {
            return;
        }
        log.info("Probing target: {}", targetKey);
        targetService.check(targetKey);
        log.info("Target probed: {}", targetKey);
    }
}
