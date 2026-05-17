package com.gcsc.studentcenter.config;

import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

  private final AppUserRepository appUserRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.init-admin.username:bdai}")
  private String initAdminUsername;

  @Value("${app.init-admin.password:bdai2026}")
  private String initAdminPassword;

  @Value("${app.init-admin.display-name:管理员}")
  private String initAdminDisplayName;

  public DataInitializer(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
    this.appUserRepository = appUserRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    if (appUserRepository.count() > 0) {
      log.info("数据库已有用户，跳过初始管理员创建");
      return;
    }

    AppUser admin = new AppUser();
    admin.setUsername(initAdminUsername);
    admin.setPasswordHash(passwordEncoder.encode(initAdminPassword));
    admin.setDisplayName(initAdminDisplayName);
    admin.setRole(UserRole.ADMIN);
    admin.setCreatedAt(LocalDateTime.now());

    appUserRepository.save(admin);
    log.info("已创建初始管理员账户: username={}", initAdminUsername);
  }
}
