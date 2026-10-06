package com.project.uptime_status.schedule;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.uptime_status.queue.ProbeQueue;
import com.project.uptime_status.service.TargetService;

@Component
@Profile("!worker")
public class TargetProbeScheduler {

    private final TargetService targetService;
    private final ProbeQueue probeQueue;

    public TargetProbeScheduler(TargetService targetService, ProbeQueue probeQueue) {
        this.targetService = targetService;
        this.probeQueue = probeQueue;
    }

    @Scheduled(fixedDelayString = "${uptime.probe-interval-ms}")
    public void probeAllTargets() {

        targetService.targetCatalog().keySet().forEach(probeQueue::enqueue);
    }
}
