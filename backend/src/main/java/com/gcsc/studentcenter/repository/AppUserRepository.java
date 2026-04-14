package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {
    boolean existsByUsername(String username);

    Optional<AppUser> findByUsername(String username);
}
