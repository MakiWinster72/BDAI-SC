package com.gcsc.studentcenter.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gcsc.studentcenter.dto.StudentProfileRequest;
import com.gcsc.studentcenter.dto.StudentProfileResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.StudentProfile;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.StudentProfileRepository;

@Service
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final AppUserRepository appUserRepository;

    public StudentProfileService(
        StudentProfileRepository studentProfileRepository,
        AppUserRepository appUserRepository
    ) {
        this.studentProfileRepository = studentProfileRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(String username) {
        AppUser user = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Optional<StudentProfile> profileOpt = studentProfileRepository.findByUserId(user.getId());
        return toResponse(user, profileOpt.orElse(null));
    }

    @Transactional
    public StudentProfileResponse saveProfile(String username, StudentProfileRequest request) {
        AppUser user = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        StudentProfile profile = studentProfileRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                StudentProfile created = new StudentProfile();
                created.setUser(user);
                return created;
            });

        profile.setFullName(normalize(request.getFullName()));
        profile.setAvatarUrl(normalize(request.getAvatarUrl()));
        profile.setStudentNo(normalize(request.getStudentNo()));
        profile.setClassYear(request.getClassYear());
        profile.setClassMajor(normalize(request.getClassMajor()));
        profile.setClassNo(normalize(request.getClassNo()));
        profile.setClassName(resolveClassName(request));
        profile.setCollege(normalize(request.getCollege()));
        profile.setPhone(normalize(request.getPhone()));
        profile.setAddress(normalize(request.getAddress()));
        profile.setIdNo(normalize(request.getIdNo()));
        profile.setNativePlace(normalize(request.getNativePlace()));
        profile.setLeagueNo(normalize(request.getLeagueNo()));
        profile.setPartyApplied(request.getPartyApplied());
        profile.setNotDeveloped(request.getNotDeveloped());
        profile.setApplicationDate(request.getApplicationDate());
        profile.setActivistDate(request.getActivistDate());
        profile.setEmergencyPhone(normalize(request.getEmergencyPhone()));
        profile.setEmergencyRelation(normalize(request.getEmergencyRelation()));

        syncUserSummary(user, profile);

        StudentProfile saved = studentProfileRepository.save(profile);
        return toResponse(user, saved);
    }

    private void syncUserSummary(AppUser user, StudentProfile profile) {
        String displayName = normalize(profile.getFullName());
        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        user.setStudentNo(profile.getStudentNo());
        user.setClassName(profile.getClassName());
        if (profile.getCollege() != null) {
            user.setCollege(profile.getCollege());
        }
        appUserRepository.save(user);
    }

    private StudentProfileResponse toResponse(AppUser user, StudentProfile profile) {
        return new StudentProfileResponse(
            profile != null ? profile.getId() : null,
            user.getUsername(),
            user.getDisplayName(),
            user.getStudentNo(),
            user.getClassName(),
            user.getCollege(),
            profile != null ? profile.getFullName() : user.getDisplayName(),
            profile != null ? profile.getAvatarUrl() : null,
            profile != null ? profile.getClassYear() : null,
            profile != null ? profile.getClassMajor() : null,
            profile != null ? profile.getClassNo() : null,
            profile != null ? profile.getPhone() : null,
            profile != null ? profile.getAddress() : null,
            profile != null ? profile.getIdNo() : null,
            profile != null ? profile.getNativePlace() : null,
            profile != null ? profile.getLeagueNo() : null,
            profile != null ? profile.getPartyApplied() : null,
            profile != null ? profile.getNotDeveloped() : null,
            profile != null ? profile.getApplicationDate() : null,
            profile != null ? profile.getActivistDate() : null,
            profile != null ? profile.getEmergencyPhone() : null,
            profile != null ? profile.getEmergencyRelation() : null
        );
    }

    private String resolveClassName(StudentProfileRequest request) {
        if (request.getClassName() != null && !request.getClassName().trim().isEmpty()) {
            return request.getClassName().trim();
        }
        String year = request.getClassYear() == null ? "" : request.getClassYear().toString();
        String major = normalize(request.getClassMajor());
        String no = normalize(request.getClassNo());
        if (year.isEmpty() && major == null && no == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (!year.isEmpty()) {
            builder.append(year).append("级");
        }
        if (major != null) {
            builder.append(major);
        }
        if (no != null) {
            builder.append(no).append("班");
        }
        return builder.toString();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
