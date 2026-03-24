package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AchievementIeerTraining;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementIeerTrainingRepository extends JpaRepository<AchievementIeerTraining, Long> {
    List<AchievementIeerTraining> findAllByAuthor_UsernameOrderByCreatedAtDesc(String username);

    List<AchievementIeerTraining> findAllByStudentNoOrderByCreatedAtDesc(String studentNo);

    List<AchievementIeerTraining> findAllByStudentNameOrderByCreatedAtDesc(String studentName);

    List<AchievementIeerTraining> findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(
        String studentNo,
        String studentName
    );
}
