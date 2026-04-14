package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
