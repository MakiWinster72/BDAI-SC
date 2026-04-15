package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.CreateUserRequest;
import com.gcsc.studentcenter.dto.UpdateUserRequest;
import com.gcsc.studentcenter.dto.UserListItemResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AppUserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new RuntimeException("用户名已存在");
        }
        AppUser user = new AppUser();
        user.setUsername(request.getUsername().trim());
        user.setDisplayName(request.getDisplayName() != null ? request.getDisplayName().trim() : request.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.STUDENT);
        user.setStudentNo(request.getStudentNo() != null ? request.getStudentNo().trim() : null);
        user.setClassName(request.getClassName() != null ? request.getClassName().trim() : null);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public Map<String, Object> listUsersPaginated(int page, int size, String search, String role, String className, String excludeUsername) {
        Specification<AppUser> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("displayName")), pattern),
                        cb.like(cb.lower(root.get("studentNo")), pattern),
                        cb.like(cb.lower(root.get("className")), pattern)
                ));
            }
            if (role != null && !role.isBlank()) {
                preds.add(cb.equal(root.get("role"), role));
            }
            if (className != null && !className.isBlank()) {
                preds.add(cb.like(cb.lower(root.get("className")), "%" + className.trim().toLowerCase() + "%"));
            }
            if (excludeUsername != null && !excludeUsername.isBlank()) {
                preds.add(cb.notEqual(root.get("username"), excludeUsername));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };

        Page<AppUser> userPage = userRepository.findAll(spec, PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));

        List<UserListItemResponse> items = userPage.getContent().stream()
                .map(UserListItemResponse::new)
                .collect(Collectors.toList());
        return Map.of(
                "data", items,
                "total", userPage.getTotalElements(),
                "page", page,
                "size", size,
                "pages", userPage.getTotalPages()
        );
    }

    public Map<String, Object> listUsersPaginated(int page, int size, String search, String role, String className) {
        return listUsersPaginated(page, size, search, role, className, null);
    }

    public List<Long> listAllUserIds(String search, String role, String className, String excludeUsername) {
        Specification<AppUser> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("displayName")), pattern),
                        cb.like(cb.lower(root.get("studentNo")), pattern),
                        cb.like(cb.lower(root.get("className")), pattern)
                ));
            }
            if (role != null && !role.isBlank()) {
                preds.add(cb.equal(root.get("role"), role));
            }
            if (className != null && !className.isBlank()) {
                preds.add(cb.like(cb.lower(root.get("className")), "%" + className.trim().toLowerCase() + "%"));
            }
            if (excludeUsername != null && !excludeUsername.isBlank()) {
                preds.add(cb.notEqual(root.get("username"), excludeUsername));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };
        return userRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(AppUser::getId)
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
