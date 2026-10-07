package com.project.uptime_status.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "check_result_history")
public class CheckResultHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "latency_ms", nullable = false)
    private int latencyMs;

    @Column(name = "status_code", nullable = true)
    private Integer statusCode;

    @Column(name = "up", nullable = false)
    private boolean up;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    protected CheckResultHistoryEntity() {
    }

    public CheckResultHistoryEntity(String slug, Instant observedAt, int latencyMs, Integer statusCode, boolean up) {
        this.slug = slug;
        this.observedAt = observedAt;
        this.latencyMs = latencyMs;
        this.statusCode = statusCode;
        this.up = up;
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }
    
    public Instant getObservedAt() {
        return observedAt;
    }

    public int getLatencyMs() {
        return latencyMs;
    }
    
    public Integer getStatusCode() {
        return statusCode;
    }

    public boolean isUp() {
        return up;
    }
}
