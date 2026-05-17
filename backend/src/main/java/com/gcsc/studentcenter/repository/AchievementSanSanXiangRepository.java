package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AchievementSanSanXiang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementSanSanXiangRepository extends JpaRepository<AchievementSanSanXiang, Long> {
  List<AchievementSanSanXiang> findAllByAuthor_UsernameOrderByCreatedAtDesc(String username);

  List<AchievementSanSanXiang> findAllByStudentNoOrderByCreatedAtDesc(String studentNo);

  List<AchievementSanSanXiang> findAllByStudentNameOrderByCreatedAtDesc(String studentName);

  List<AchievementSanSanXiang> findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(
      String studentNo,
      String studentName);
}