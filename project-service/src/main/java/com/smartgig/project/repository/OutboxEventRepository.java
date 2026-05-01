package com.smartgig.project.repository;

import com.smartgig.project.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    @Query("SELECT e FROM OutboxEvent e WHERE e.publishedAt IS NULL ORDER BY e.createdAt ASC")
    List<OutboxEvent> findUnpublished();

    @Query("SELECT e FROM OutboxEvent e WHERE e.publishedAt IS NULL AND e.createdAt >= :since ORDER BY e.createdAt ASC")
    List<OutboxEvent> findUnpublishedSince(@Param("since") LocalDateTime since);
}

