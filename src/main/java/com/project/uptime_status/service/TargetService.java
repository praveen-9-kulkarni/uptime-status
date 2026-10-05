package com.project.uptime_status.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.uptime_status.exception.UnknownTargetException;
import com.project.uptime_status.persistence.CheckResultEntity;
import com.project.uptime_status.repository.CheckResultRepository;

@Service
public class TargetService {

    private static final Logger log = LoggerFactory.getLogger(TargetService.class);

    public record Target(String name, String url) {}

    private final Map<String, Target> TARGETS = Map.of(
        "github", new Target("GitHub", "https://github.com")
    );

    public record CheckResult(boolean up, Integer statusCode, long latencyMs, Instant observedAt) {}

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    private final CheckResultRepository checkResultRepository;

    public TargetService(CheckResultRepository checkResultRepository) {
        this.checkResultRepository = checkResultRepository;
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

        Instant start = Instant.now();
        try {
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .timeout(Duration.ofSeconds(5))
            .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return new CheckResult(response.statusCode() < 400, response.statusCode(), Duration.between(start, Instant.now()).toMillis(), start);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new CheckResult(false, null, Duration.between(start, Instant.now()).toMillis(), start);
        } catch (IOException e) {
            return new CheckResult(false, null, Duration.between(start, Instant.now()).toMillis(), start);
        }
    }

    @Transactional
    public CheckResult check(String key) {
    
        Target target = resolveTargetOrThrow(key);
        CheckResult result = probe(target.url());
        checkResultRepository.save(toEntity(key, result));
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
}
