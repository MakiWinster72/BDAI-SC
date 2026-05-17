package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AchievementReviewRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementReviewRequestRepository extends JpaRepository<AchievementReviewRequest, Long> {
  List<AchievementReviewRequest> findAllByRequester_UsernameOrderByUpdatedAtDesc(String username);

  List<AchievementReviewRequest> findAllByOrderByUpdatedAtDesc();
}
