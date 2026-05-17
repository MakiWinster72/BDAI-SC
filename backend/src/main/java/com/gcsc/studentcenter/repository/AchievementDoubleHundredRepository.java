package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AchievementDoubleHundred;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementDoubleHundredRepository extends JpaRepository<AchievementDoubleHundred, Long> {
  List<AchievementDoubleHundred> findAllByAuthor_UsernameOrderByCreatedAtDesc(String username);

  List<AchievementDoubleHundred> findAllByStudentNoOrderByCreatedAtDesc(String studentNo);

  List<AchievementDoubleHundred> findAllByStudentNameOrderByCreatedAtDesc(String studentName);

  List<AchievementDoubleHundred> findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(
      String studentNo,
      String studentName);
}
