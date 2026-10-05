package com.project.uptime_status.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.uptime_status.service.TargetService;

@Component
public class TargetProbeScheduler {

    private final TargetService targetService;

    public TargetProbeScheduler(TargetService targetService) {
        this.targetService = targetService;
    }

    @Scheduled(fixedDelayString = "${uptime.probe-interval-ms}")
    public void probeAllTargets() {

        targetService.checkAll();
    }
}
