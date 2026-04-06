package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AchievementResearch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementResearchRepository extends JpaRepository<AchievementResearch, Long> {
    List<AchievementResearch> findAllByAuthor_UsernameOrderByCreatedAtDesc(String username);

    List<AchievementResearch> findAllByStudentNoOrderByCreatedAtDesc(String studentNo);

    List<AchievementResearch> findAllByStudentNameOrderByCreatedAtDesc(String studentName);

    List<AchievementResearch> findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(
        String studentNo,
        String studentName
    );
}
