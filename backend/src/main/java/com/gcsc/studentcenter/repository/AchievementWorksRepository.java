package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AchievementWorks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementWorksRepository extends JpaRepository<AchievementWorks, Long> {
    List<AchievementWorks> findAllByAuthor_UsernameOrderByCreatedAtDesc(String username);

    List<AchievementWorks> findAllByStudentNoOrderByCreatedAtDesc(String studentNo);

    List<AchievementWorks> findAllByStudentNameOrderByCreatedAtDesc(String studentName);

    List<AchievementWorks> findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(
        String studentNo,
        String studentName
    );
}
