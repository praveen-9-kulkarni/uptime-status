package com.project.uptime_status.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "check_result")
public class CheckResultEntity {

    @Id
    private String slug;

    @Column(name = "up", nullable = false)
    private boolean up;

    @Column(name = "status_code", nullable = true)
    private Integer statusCode;

    @Column(name = "latency_ms", nullable = false)
    private int latencyMs;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    protected CheckResultEntity() {
    }

    public CheckResultEntity(String slug, boolean up, Integer statusCode, int latencyMs, Instant observedAt) {
        this.slug = slug;
        this.up = up;
        this.statusCode = statusCode;
        this.latencyMs = latencyMs;
        this.observedAt = observedAt;
    }

    public String getSlug() {
        return slug;
    }

    public boolean isUp() {
        return up;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public int getLatencyMs() {
        return latencyMs;
    }

    public Instant getObservedAt() {
        return observedAt;
    }
}
