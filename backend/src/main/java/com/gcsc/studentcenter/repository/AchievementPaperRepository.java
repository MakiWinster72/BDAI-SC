package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AchievementPaper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementPaperRepository extends JpaRepository<AchievementPaper, Long> {
    List<AchievementPaper> findAllByAuthor_UsernameOrderByCreatedAtDesc(String username);

    List<AchievementPaper> findAllByStudentNoOrderByCreatedAtDesc(String studentNo);

    List<AchievementPaper> findAllByStudentNameOrderByCreatedAtDesc(String studentName);

    List<AchievementPaper> findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(
        String studentNo,
        String studentName
    );
}
