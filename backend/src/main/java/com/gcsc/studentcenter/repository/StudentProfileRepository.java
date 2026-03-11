package com.gcsc.studentcenter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gcsc.studentcenter.entity.StudentProfile;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);
}
