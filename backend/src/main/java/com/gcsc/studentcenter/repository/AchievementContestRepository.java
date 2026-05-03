package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AchievementContest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementContestRepository extends JpaRepository<AchievementContest, Long> {
  List<AchievementContest> findAllByAuthor_UsernameOrderByCreatedAtDesc(String username);

  List<AchievementContest> findAllByStudentNoOrderByCreatedAtDesc(String studentNo);

  List<AchievementContest> findAllByStudentNameOrderByCreatedAtDesc(String studentName);

  List<AchievementContest> findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(
      String studentNo,
      String studentName);
}
