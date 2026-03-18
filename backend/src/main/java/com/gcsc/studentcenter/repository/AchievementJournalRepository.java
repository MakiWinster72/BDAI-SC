package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AchievementJournal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementJournalRepository extends JpaRepository<AchievementJournal, Long> {
    List<AchievementJournal> findAllByAuthor_UsernameOrderByCreatedAtDesc(String username);
}
