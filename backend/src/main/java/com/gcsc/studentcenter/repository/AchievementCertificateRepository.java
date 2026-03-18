package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AchievementCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementCertificateRepository extends JpaRepository<AchievementCertificate, Long> {
    List<AchievementCertificate> findAllByAuthor_UsernameOrderByCreatedAtDesc(String username);

    List<AchievementCertificate> findAllByStudentNoOrderByCreatedAtDesc(String studentNo);

    List<AchievementCertificate> findAllByStudentNameOrderByCreatedAtDesc(String studentName);

    List<AchievementCertificate> findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(
        String studentNo,
        String studentName
    );
}
