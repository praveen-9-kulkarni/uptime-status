package com.project.uptime_status.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.uptime_status.service.TargetService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
@Profile("worker")
public class ProbeWorker {

    private final Logger log = LoggerFactory.getLogger(ProbeWorker.class);
    private final ProbeQueue probeQueue;
    private final TargetService targetService;
    private final Counter errorCounter;

    public ProbeWorker(ProbeQueue probeQueue, TargetService targetService, MeterRegistry meterRegistry) {

        this.probeQueue = probeQueue;
        this.targetService = targetService;
        this.errorCounter = Counter.builder("uptime.worker.errors")
                                   .description("Total number of errors encountered during probe execution")
                                   .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 1000)
    public void work() {

        String targetKey = probeQueue.dequeue();
        if (targetKey == null) {
            return;
        }
        log.info("Probing target: {}", targetKey);
        try {
            targetService.check(targetKey);
            log.info("Target probed: {}", targetKey);
        } catch (Exception e) {
            errorCounter.increment();
            log.error("Error probing target: {}", targetKey, e);
        }
    }
}
