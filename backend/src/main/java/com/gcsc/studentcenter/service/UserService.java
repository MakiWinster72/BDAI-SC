package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.UpdateUserRequest;
import com.gcsc.studentcenter.dto.UserListItemResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserListItemResponse> listAllUsers() {
        return userRepository.findAll().stream()
                .map(UserListItemResponse::new)
                .collect(Collectors.toList());
    }

    public AppUser updateUser(Long userId, UpdateUserRequest request) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername().trim();
            if (!newUsername.equals(user.getUsername())) {
                if (userRepository.existsByUsername(newUsername)) {
                    throw new RuntimeException("用户名已存在");
                }
                user.setUsername(newUsername);
            }
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(userId);
    }
}
