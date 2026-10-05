package com.project.uptime_status.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.uptime_status.persistence.CheckResultEntity;

public interface CheckResultRepository extends JpaRepository<CheckResultEntity, String> {
}
