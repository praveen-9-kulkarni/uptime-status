package com.project.uptime_status.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.uptime_status.service.TargetService;

@Component
@Profile("!worker")
public class HistoryPruneScheduler {

    private final Logger log = LoggerFactory.getLogger(HistoryPruneScheduler.class);
    private final TargetService targetService;
    private final int historyKeepPerSlug;

    public HistoryPruneScheduler(
            TargetService targetService,
            @Value("${uptime.history-keep-per-slug}") int historyKeepPerSlug) {
        this.targetService = targetService;
        this.historyKeepPerSlug = historyKeepPerSlug;
    }

    @Scheduled(fixedDelayString = "${uptime.history-prune-interval-ms}")
    public void pruneHistory() {

        log.info("Pruning history");
        targetService.targetCatalog().keySet()
                .forEach(targetKey -> targetService.pruneHistory(targetKey, historyKeepPerSlug));
    }
}
