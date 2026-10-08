package com.project.uptime_status.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.uptime_status.persistence.CheckResultHistoryEntity;

public interface CheckResultHistoryRepository extends JpaRepository<CheckResultHistoryEntity, Long> {

    List<CheckResultHistoryEntity> findBySlugOrderByObservedAtDesc(String slug, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query(
            value = """
                    DELETE FROM check_result_history h
                    USING (
                        SELECT id
                        FROM check_result_history
                        WHERE slug = :slug
                        ORDER BY observed_at DESC, id DESC
                        OFFSET :k
                    ) old
                    WHERE h.id = old.id
                    """,
            nativeQuery = true)
    void deleteAllButFirstKPerSlug(@Param("slug") String slug, @Param("k") int k);
}
