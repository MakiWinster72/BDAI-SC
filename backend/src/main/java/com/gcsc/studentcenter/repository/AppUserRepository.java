package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {
  boolean existsByUsername(String username);

  Optional<AppUser> findByUsername(String username);

  @Query("SELECT DISTINCT u.className FROM AppUser u WHERE u.role = :role AND u.className IS NOT NULL AND u.className <> '' ORDER BY u.className")
  List<String> findDistinctClassNamesByRole(UserRole role);

  List<AppUser> findByUsernameIn(Collection<String> usernames);
}
