package com.gcsc.studentcenter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gcsc.studentcenter.dto.StudentProfileRequest;
import com.gcsc.studentcenter.dto.StudentProfileResponse;
import com.gcsc.studentcenter.dto.StudentSearchItemResponse;
import com.gcsc.studentcenter.dto.StudentSearchResponse;
import com.gcsc.studentcenter.dto.CadreExperienceItem;
import com.gcsc.studentcenter.dto.EducationExperienceItem;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.CadreExperience;
import com.gcsc.studentcenter.entity.EducationExperience;
import com.gcsc.studentcenter.entity.StudentProfile;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.StudentProfileRepository;

@Service
public class StudentProfileService {

    private static final String FIXED_COLLEGE = "大数据与人工智能学院";

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

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfileById(Long id) {
        StudentProfile profile = studentProfileRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("学生档案不存在"));
        AppUser user = profile.getUser();
        return toResponse(user, profile);
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
        profile.setStudentNo(normalize(request.getStudentNo()));
        profile.setClassYear(request.getClassYear());
        profile.setClassMajor(normalize(request.getClassMajor()));
        profile.setClassNo(normalize(request.getClassNo()));
        profile.setClassName(resolveClassName(request));
        profile.setCollege(FIXED_COLLEGE);
        profile.setEnrollmentDate(request.getEnrollmentDate());
        profile.setStudentCategory(normalize(request.getStudentCategory()));
        profile.setEthnicity(normalize(request.getEthnicity()));
        profile.setPoliticalStatus(normalize(request.getPoliticalStatus()));
        profile.setDormCampus(normalize(request.getDormCampus()));
        profile.setDormBuilding(normalize(request.getDormBuilding()));
        profile.setDormRoom(normalize(request.getDormRoom()));
        profile.setOffCampusLiving(request.getOffCampusLiving());
        profile.setOffCampusAddress(normalize(request.getOffCampusAddress()));
        profile.setClassTeacher(normalize(request.getClassTeacher()));
        profile.setCounselor(normalize(request.getCounselor()));
        profile.setPhone(normalize(request.getPhone()));
        profile.setBackupContact(normalize(request.getBackupContact()));
        profile.setAddress(normalize(request.getAddress()));
        profile.setIdNo(normalize(request.getIdNo()));
        profile.setBirthDate(request.getBirthDate());
        profile.setNativePlace(normalize(request.getNativePlace()));
        profile.setLeagueNo(normalize(request.getLeagueNo()));
        profile.setLeagueApplicationDate(request.getLeagueApplicationDate());
        profile.setLeagueJoinDate(request.getLeagueJoinDate());
        profile.setLeagueJoined(request.getLeagueJoined());
        profile.setLeagueDeveloping(request.getLeagueDeveloping());
        profile.setPartyApplied(request.getPartyApplied());
        profile.setNotDeveloped(request.getNotDeveloped());
        profile.setApplicationDate(request.getApplicationDate());
        profile.setActivistDate(request.getActivistDate());
        profile.setActivistDeveloping(request.getActivistDeveloping());
        profile.setPartyTrainingDate(request.getPartyTrainingDate());
        profile.setPartyTrainingPending(request.getPartyTrainingPending());
        profile.setDevelopmentTargetDate(request.getDevelopmentTargetDate());
        profile.setDevelopmentTargetDeveloping(request.getDevelopmentTargetDeveloping());
        profile.setProbationaryMemberDate(request.getProbationaryMemberDate());
        profile.setProbationaryDeveloping(request.getProbationaryDeveloping());
        profile.setFullMemberDate(request.getFullMemberDate());
        profile.setFullMemberDeveloping(request.getFullMemberDeveloping());
        profile.setEmergencyPhone(normalize(request.getEmergencyPhone()));
        profile.setEmergencyRelation(normalize(request.getEmergencyRelation()));
        profile.setHkMoTw(request.getHkMoTw());
        profile.setSpecialStudent(request.getSpecialStudent());
        profile.setFatherName(normalize(request.getFatherName()));
        profile.setFatherPhone(normalize(request.getFatherPhone()));
        profile.setFatherWorkUnit(normalize(request.getFatherWorkUnit()));
        profile.setFatherTitle(normalize(request.getFatherTitle()));
        profile.setMotherName(normalize(request.getMotherName()));
        profile.setMotherPhone(normalize(request.getMotherPhone()));
        profile.setMotherWorkUnit(normalize(request.getMotherWorkUnit()));
        profile.setMotherTitle(normalize(request.getMotherTitle()));
        syncEducationExperiences(profile, request.getEducationExperiences());
        syncCadreExperiences(profile, request.getCadreExperiences());

        user.setAvatarUrl(normalize(request.getAvatarUrl()));
        syncUserSummary(user, profile);

        StudentProfile saved = studentProfileRepository.save(profile);
        return toResponse(user, saved);
    }

    @Transactional(readOnly = true)
    public StudentSearchResponse searchProfiles(
        Integer classYear,
        String classNo,
        String college,
        String major,
        Boolean hkMoTw,
        Boolean specialStudent,
        String keyword,
        int page,
        int size
    ) {
        int pageIndex = Math.max(page - 1, 0);
        int pageSize = Math.max(size, 1);
        Page<StudentSearchItemResponse> result = studentProfileRepository.searchProfiles(
            classYear,
            normalize(classNo),
            normalize(college),
            normalize(major),
            hkMoTw,
            specialStudent,
            normalize(keyword),
            PageRequest.of(pageIndex, pageSize)
        );
        return new StudentSearchResponse(
            result.getContent(),
            pageIndex + 1,
            pageSize,
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    private void syncUserSummary(AppUser user, StudentProfile profile) {
        String displayName = normalize(profile.getFullName());
        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        user.setStudentNo(profile.getStudentNo());
        user.setClassName(profile.getClassName());
        user.setCollege(FIXED_COLLEGE);
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
            user.getAvatarUrl(),
            profile != null ? profile.getClassYear() : null,
            profile != null ? profile.getClassMajor() : null,
            profile != null ? profile.getClassNo() : null,
            profile != null ? profile.getEnrollmentDate() : null,
            profile != null ? profile.getStudentCategory() : null,
            profile != null ? profile.getEthnicity() : null,
            profile != null ? profile.getPoliticalStatus() : null,
            profile != null ? profile.getPhone() : null,
            profile != null ? profile.getBackupContact() : null,
            profile != null ? profile.getAddress() : null,
            profile != null ? profile.getIdNo() : null,
            profile != null ? profile.getBirthDate() : null,
            profile != null ? profile.getNativePlace() : null,
            profile != null ? profile.getDormCampus() : null,
            profile != null ? profile.getDormBuilding() : null,
            profile != null ? profile.getDormRoom() : null,
            profile != null ? profile.getOffCampusLiving() : null,
            profile != null ? profile.getOffCampusAddress() : null,
            profile != null ? profile.getClassTeacher() : null,
            profile != null ? profile.getCounselor() : null,
            profile != null ? profile.getLeagueNo() : null,
            profile != null ? profile.getLeagueApplicationDate() : null,
            profile != null ? profile.getLeagueJoinDate() : null,
            profile != null ? profile.getLeagueJoined() : null,
            profile != null ? profile.getLeagueDeveloping() : null,
            profile != null ? profile.getPartyApplied() : null,
            profile != null ? profile.getNotDeveloped() : null,
            profile != null ? profile.getApplicationDate() : null,
            profile != null ? profile.getActivistDate() : null,
            profile != null ? profile.getActivistDeveloping() : null,
            profile != null ? profile.getPartyTrainingDate() : null,
            profile != null ? profile.getPartyTrainingPending() : null,
            profile != null ? profile.getDevelopmentTargetDate() : null,
            profile != null ? profile.getDevelopmentTargetDeveloping() : null,
            profile != null ? profile.getProbationaryMemberDate() : null,
            profile != null ? profile.getProbationaryDeveloping() : null,
            profile != null ? profile.getFullMemberDate() : null,
            profile != null ? profile.getFullMemberDeveloping() : null,
            profile != null ? profile.getEmergencyPhone() : null,
            profile != null ? profile.getEmergencyRelation() : null,
            profile != null ? profile.getHkMoTw() : null,
            profile != null ? profile.getSpecialStudent() : null,
            profile != null ? profile.getFatherName() : null,
            profile != null ? profile.getFatherPhone() : null,
            profile != null ? profile.getFatherWorkUnit() : null,
            profile != null ? profile.getFatherTitle() : null,
            profile != null ? profile.getMotherName() : null,
            profile != null ? profile.getMotherPhone() : null,
            profile != null ? profile.getMotherWorkUnit() : null,
            profile != null ? profile.getMotherTitle() : null,
            toEducationItems(profile != null ? profile.getEducationExperiences() : null),
            toCadreItems(profile != null ? profile.getCadreExperiences() : null)
        );
    }

    private void syncEducationExperiences(
        StudentProfile profile,
        List<EducationExperienceItem> educationExperiences
    ) {
        List<EducationExperience> target = profile.getEducationExperiences();
        target.clear();
        if (educationExperiences == null) {
            return;
        }
        for (EducationExperienceItem item : educationExperiences) {
            EducationExperience experience = new EducationExperience();
            experience.setProfile(profile);
            experience.setStartDate(item.getStartDate());
            experience.setEndDate(Boolean.TRUE.equals(item.getIsCurrent()) ? null : item.getEndDate());
            experience.setSchoolName(normalize(item.getSchoolName()));
            experience.setEducationLevel(normalize(item.getEducationLevel()));
            experience.setWitness(normalize(item.getWitness()));
            experience.setIsCurrent(item.getIsCurrent());
            target.add(experience);
        }
    }

    private List<EducationExperienceItem> toEducationItems(List<EducationExperience> experiences) {
        if (experiences == null) {
            return null;
        }
        List<EducationExperienceItem> items = new ArrayList<>();
        for (EducationExperience experience : experiences) {
            EducationExperienceItem item = new EducationExperienceItem();
            item.setStartDate(experience.getStartDate());
            item.setEndDate(experience.getEndDate());
            item.setSchoolName(experience.getSchoolName());
            item.setEducationLevel(experience.getEducationLevel());
            item.setWitness(experience.getWitness());
            item.setIsCurrent(experience.getIsCurrent());
            items.add(item);
        }
        return items;
    }

    private void syncCadreExperiences(
        StudentProfile profile,
        List<CadreExperienceItem> cadreExperiences
    ) {
        List<CadreExperience> target = profile.getCadreExperiences();
        target.clear();
        if (cadreExperiences == null) {
            return;
        }
        for (CadreExperienceItem item : cadreExperiences) {
            CadreExperience experience = new CadreExperience();
            experience.setProfile(profile);
            experience.setStartDate(item.getStartDate());
            experience.setEndDate(Boolean.TRUE.equals(item.getIsCurrent()) ? null : item.getEndDate());
            experience.setDepartment(normalize(item.getDepartment()));
            experience.setPosition(normalize(item.getPosition()));
            experience.setDescription(normalize(item.getDescription()));
            experience.setIsCurrent(item.getIsCurrent());
            target.add(experience);
        }
    }

    private List<CadreExperienceItem> toCadreItems(List<CadreExperience> experiences) {
        if (experiences == null) {
            return null;
        }
        List<CadreExperienceItem> items = new ArrayList<>();
        for (CadreExperience experience : experiences) {
            CadreExperienceItem item = new CadreExperienceItem();
            item.setStartDate(experience.getStartDate());
            item.setEndDate(experience.getEndDate());
            item.setDepartment(experience.getDepartment());
            item.setPosition(experience.getPosition());
            item.setDescription(experience.getDescription());
            item.setIsCurrent(experience.getIsCurrent());
            items.add(item);
        }
        return items;
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
