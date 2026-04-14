package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.ProfileReviewRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileReviewRequestRepository extends JpaRepository<ProfileReviewRequest, Long> {

    List<ProfileReviewRequest> findAllByOrderByUpdatedAtDesc();

    List<ProfileReviewRequest> findAllByRequester_UsernameOrderByUpdatedAtDesc(String username);
}