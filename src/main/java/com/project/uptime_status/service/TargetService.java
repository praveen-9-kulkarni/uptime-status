package com.project.uptime_status.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.uptime_status.enums.State;
import com.project.uptime_status.exception.UnknownTargetException;
import com.project.uptime_status.persistence.CheckResultEntity;
import com.project.uptime_status.persistence.CheckResultHistoryEntity;
import com.project.uptime_status.repository.CheckResultHistoryRepository;
import com.project.uptime_status.repository.CheckResultRepository;

import io.micrometer.core.instrument.MeterRegistry;

@Service
public class TargetService {

    private static final Logger log = LoggerFactory.getLogger(TargetService.class);

    public record Target(String name, String url) {}

    private final Map<String, Target> TARGETS = Map.of(
        "github", new Target("GitHub", "https://github.com"),
        "google", new Target("Google", "https://google.com")
    );

    public record CheckResult(boolean up, Integer statusCode, long latencyMs, Instant observedAt) {}

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    private final CheckResultRepository checkResultRepository;
    private final CheckResultHistoryRepository checkResultHistoryRepository;
    private final Clock clock;
    private final int probeIntervalMs;
    private final int staleAfterIntervals;
    private final int staleGraceMs;
    private final MeterRegistry meterRegistry;

    public TargetService(
            CheckResultRepository checkResultRepository,
            CheckResultHistoryRepository checkResultHistoryRepository,
            Clock clock,
            @Value("${uptime.probe-interval-ms}") int probeIntervalMs,
            @Value("${uptime.stale-after-intervals}") int staleAfterIntervals,
            @Value("${uptime.stale-grace-ms}") int staleGraceMs,
            MeterRegistry meterRegistry) {

        this.checkResultRepository = checkResultRepository;
        this.checkResultHistoryRepository = checkResultHistoryRepository;
        this.clock = clock;
        this.probeIntervalMs = probeIntervalMs;
        this.staleAfterIntervals = staleAfterIntervals;
        this.staleGraceMs = staleGraceMs;
        this.meterRegistry = meterRegistry;
    }

    public Target getTarget(String key) {

        Target target = resolveTargetOrThrow(key);
        return target;
    }

    private Target resolveTargetOrThrow(String key) {
    
        Target target = TARGETS.get(key);
        if (target == null) {
            throw new UnknownTargetException(key);
        }
        return target;
    }

    private CheckResult probe(String url) {

        Instant start = clock.instant();
        try {
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .timeout(Duration.ofSeconds(5))
            .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return new CheckResult(response.statusCode() < 400, response.statusCode(), Duration.between(start, clock.instant()).toMillis(), start);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new CheckResult(false, null, Duration.between(start, clock.instant()).toMillis(), start);
        } catch (IOException e) {
            return new CheckResult(false, null, Duration.between(start, clock.instant()).toMillis(), start);
        }
    }

    @Transactional
    public CheckResult check(String key) {
    
        Target target = resolveTargetOrThrow(key);
        CheckResult result = probe(target.url());
        meterRegistry.counter("uptime.probe", "slug", key, "outcome", result.up() ? "up" : "down").increment();
        meterRegistry.timer("uptime.probe.latency", "slug", key).record(result.latencyMs(), TimeUnit.MILLISECONDS);
        checkResultRepository.save(toEntity(key, result));
        checkResultHistoryRepository.save(toHistoryEntity(key, result));
        return result;
    }

    public CheckResult lastCheck(String key) {
       
        resolveTargetOrThrow(key);
        return checkResultRepository.findById(key)
                .map(this::toDomain)
                .orElse(null);
    }

    public Map<String, Target> targetCatalog() {

        return TARGETS;
    }

    public void checkAll() {

        for (String key : TARGETS.keySet()) {
            try {
                check(key);
            } catch (UnknownTargetException e) {
                log.warn("Error checking target {}", key, e);
            }
        }
    }

    public List<CheckResult> recentHistory(String key, int limit) {

        resolveTargetOrThrow(key);
        return checkResultHistoryRepository.findBySlugOrderByObservedAtDesc(key, PageRequest.of(0, limit))
            .stream()
            .map(this::toHistoryDomain)
            .toList();
    }

    private CheckResultEntity toEntity(String key, CheckResult result) {

        return new CheckResultEntity(
                key,
                result.up(),
                result.statusCode(),
                (int) result.latencyMs(),
                result.observedAt());
    }

    private CheckResult toDomain(CheckResultEntity entity) {

        return new CheckResult(
                entity.isUp(),
                entity.getStatusCode(),
                entity.getLatencyMs(),
                entity.getObservedAt());
    }

    private CheckResultHistoryEntity toHistoryEntity(String key, CheckResult result) {
        
        return new CheckResultHistoryEntity(
            key, 
            result.observedAt(),
            (int) result.latencyMs(),
            result.statusCode(),
            result.up());
    }

    private CheckResult toHistoryDomain(CheckResultHistoryEntity entity) {

        return new CheckResult(
            entity.isUp(),
            entity.getStatusCode(),
            entity.getLatencyMs(),
            entity.getObservedAt());
    }

    @Transactional
    public void pruneHistory(String targetKey, int k) {
        resolveTargetOrThrow(targetKey);
        if (k <= 0) {
            throw new IllegalArgumentException("history keep count must be positive");
        }
        checkResultHistoryRepository.deleteAllButFirstKPerSlug(targetKey, k);
    }

    public State stateOf(CheckResult result) {

        if (result == null) return State.NO_DATA;
        long maxAgeMs = (long) staleAfterIntervals * probeIntervalMs + staleGraceMs;
        long actualAgeMs = Duration.between(result.observedAt(), clock.instant()).toMillis();
        if (actualAgeMs > maxAgeMs) return State.STALE;
        return result.up() ? State.UP : State.DOWN;
    }
}

