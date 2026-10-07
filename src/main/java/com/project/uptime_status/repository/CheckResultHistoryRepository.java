package com.project.uptime_status.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.uptime_status.persistence.CheckResultHistoryEntity;

public interface CheckResultHistoryRepository extends JpaRepository<CheckResultHistoryEntity, Long>{

    List<CheckResultHistoryEntity> findBySlugOrderByObservedAtDesc(String slug, Pageable pageable);
}
