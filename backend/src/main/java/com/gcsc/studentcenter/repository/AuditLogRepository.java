package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

  @Query("SELECT a FROM AuditLog a WHERE " +
      "(:search IS NULL OR a.username LIKE %:search% OR a.action LIKE %:search% OR a.detail LIKE %:search%) " +
      "ORDER BY a.createdAt DESC")
  Page<AuditLog> findPaginated(@Param("search") String search, Pageable pageable);
}
