package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.NotificationReadState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationReadStateRepository extends JpaRepository<NotificationReadState, Long> {

  Optional<NotificationReadState> findByUserAndResourceTypeAndResourceId(
      AppUser user,
      String resourceType,
      Long resourceId);

  List<NotificationReadState> findAllByUser_UsernameAndResourceType(String username, String resourceType);
}
