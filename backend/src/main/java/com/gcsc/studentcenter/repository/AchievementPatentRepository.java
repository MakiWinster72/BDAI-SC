package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AchievementPatent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementPatentRepository extends JpaRepository<AchievementPatent, Long> {
    List<AchievementPatent> findAllByAuthor_UsernameOrderByCreatedAtDesc(String username);
}
