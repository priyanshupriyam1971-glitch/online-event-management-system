package com.events.eventmanagement.repository;

import com.events.eventmanagement.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findTop10ByOrderByCreatedAtDesc();
    List<ActivityLog> findByOrderByCreatedAtDesc();
}
