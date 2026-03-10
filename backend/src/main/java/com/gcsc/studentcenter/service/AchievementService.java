package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.AchievementCreateRequest;
import com.gcsc.studentcenter.dto.AchievementResponse;
import com.gcsc.studentcenter.dto.AchievementUpdateRequest;
import com.gcsc.studentcenter.entity.Achievement;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.repository.AchievementRepository;
import com.gcsc.studentcenter.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final AppUserRepository appUserRepository;

    public AchievementService(AchievementRepository achievementRepository, AppUserRepository appUserRepository) {
        this.achievementRepository = achievementRepository;
        this.appUserRepository = appUserRepository;
    }

    public AchievementResponse create(String username, AchievementCreateRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("成就名称不能为空");
        }

        AppUser author = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        Achievement achievement = new Achievement();
        achievement.setAuthor(author);
        achievement.setName(name);
        achievement.setStartDate(request.getStartDate());
        achievement.setEndDate(request.getEndDate());
        achievement.setAwardDate(request.getAwardDate());
        achievement.setDescription(request.getDescription());
        achievement.setThoughts(request.getThoughts());
        achievement.setImageUrl(request.getImageUrl());
        achievement.setCreatedAt(LocalDateTime.now());

        Achievement saved = achievementRepository.save(achievement);
        return toResponse(saved);
    }

    public List<AchievementResponse> list(String username) {
        return achievementRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public AchievementResponse getById(String username, Long id) {
        Achievement achievement = achievementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        if (!achievement.getAuthor().getUsername().equals(username)) {
            throw new IllegalArgumentException("无权限查看该成就");
        }
        return toResponse(achievement);
    }

    public AchievementResponse update(String username, Long id, AchievementUpdateRequest request) {
        Achievement achievement = achievementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        if (!achievement.getAuthor().getUsername().equals(username)) {
            throw new IllegalArgumentException("无权限编辑该成就");
        }
        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("成就名称不能为空");
            }
            achievement.setName(name);
        }
        achievement.setStartDate(request.getStartDate());
        achievement.setEndDate(request.getEndDate());
        achievement.setAwardDate(request.getAwardDate());
        achievement.setDescription(request.getDescription());
        achievement.setThoughts(request.getThoughts());
        achievement.setImageUrl(request.getImageUrl());
        Achievement saved = achievementRepository.save(achievement);
        return toResponse(saved);
    }

    public void delete(String username, Long id) {
        Achievement achievement = achievementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        if (!achievement.getAuthor().getUsername().equals(username)) {
            throw new IllegalArgumentException("无权限删除该成就");
        }
        achievementRepository.delete(achievement);
    }

    private AchievementResponse toResponse(Achievement achievement) {
        return new AchievementResponse(
            achievement.getId(),
            achievement.getName(),
            achievement.getStartDate(),
            achievement.getEndDate(),
            achievement.getAwardDate(),
            achievement.getDescription(),
            achievement.getThoughts(),
            achievement.getImageUrl(),
            achievement.getCreatedAt()
        );
    }
}
