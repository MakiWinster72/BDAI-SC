package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    Page<LoginHistory> findAllByUserIdOrderByLoginTimeDesc(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM LoginHistory l WHERE l.loginTime < :cutoff")
    int deleteByLoginTimeBefore(@Param("cutoff") LocalDateTime cutoff);

    Optional<LoginHistory> findFirstByUserIdOrderByLoginTimeDesc(Long userId);

    Optional<LoginHistory> findFirstByUserIdAndIdNotOrderByLoginTimeDesc(Long userId, Long excludeId);
}
