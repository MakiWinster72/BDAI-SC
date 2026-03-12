package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.MediaItemRequest;
import com.gcsc.studentcenter.dto.MediaItemResponse;
import com.gcsc.studentcenter.dto.PostCreateRequest;
import com.gcsc.studentcenter.dto.PostResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.Post;
import com.gcsc.studentcenter.entity.PostMedia;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final AppUserRepository appUserRepository;

    public PostService(
        PostRepository postRepository,
        AppUserRepository appUserRepository
    ) {
        this.postRepository = postRepository;
        this.appUserRepository = appUserRepository;
    }

    public PostResponse createPost(String username, PostCreateRequest request) {
        String content = request.getContent() == null ? "" : request.getContent().trim();
        List<MediaItemRequest> mediaItems = request.getMedia() == null
            ? Collections.emptyList()
            : request.getMedia();

        if (content.isEmpty() && mediaItems.isEmpty()) {
            throw new IllegalArgumentException("内容或媒体至少填写一项");
        }

        AppUser author = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        Post post = new Post();
        post.setContent(content);
        post.setGoodNews(request.isGoodNews());
        post.setPrivatePost(request.isPrivatePost());
        post.setAchievement(request.isAchievement());
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());

        for (MediaItemRequest item : mediaItems) {
            if (item == null || item.getUrl() == null || item.getUrl().trim().isEmpty()) {
                continue;
            }
            PostMedia media = new PostMedia();
            media.setUrl(item.getUrl().trim());
            media.setMediaType(normalizeMediaType(item.getMediaType()));
            media.setOriginalName(item.getOriginalName());
            post.addMedia(media);
        }

        Post saved = postRepository.save(post);
        return toResponse(saved);
    }

    public List<PostResponse> listPosts(String username, String type) {
        List<Post> all = postRepository.findAllByOrderByCreatedAtDesc();
        return all.stream()
            .filter(post -> filterByType(post, username, type))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public void deletePost(String username, Long postId) {
        AppUser operator = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("动态不存在"));
        if (!canDelete(post, operator)) {
            throw new IllegalArgumentException("无权限删除该动态");
        }
        postRepository.delete(post);
    }

    private boolean filterByType(Post post, String username, String type) {
        String normalized = type == null ? "" : type.trim().toLowerCase();
        switch (normalized) {
            case "good-news":
                return post.isGoodNews() && !post.isPrivatePost();
            case "records":
                return post.isPrivatePost() && isAuthor(post, username);
            case "achievements":
                return post.isAchievement() && (!post.isPrivatePost() || isAuthor(post, username));
            case "campus":
            default:
                return !post.isPrivatePost();
        }
    }

    private boolean isAuthor(Post post, String username) {
        return post.getAuthor() != null && Objects.equals(post.getAuthor().getUsername(), username);
    }

    private boolean canDelete(Post post, AppUser operator) {
        if (post.getAuthor() != null && Objects.equals(post.getAuthor().getId(), operator.getId())) {
            return true;
        }
        UserRole role = operator.getRole();
        return role == UserRole.TEACHER || role == UserRole.ADMIN;
    }

    private PostResponse toResponse(Post post) {
        List<MediaItemResponse> media = post.getMedia() == null
            ? Collections.emptyList()
            : post.getMedia().stream()
                .map(item -> new MediaItemResponse(
                    item.getId(),
                    item.getUrl(),
                    item.getMediaType(),
                    item.getOriginalName()
                ))
                .collect(Collectors.toList());

        AppUser author = post.getAuthor();
        return new PostResponse(
            post.getId(),
            post.getContent(),
            post.isGoodNews(),
            post.isPrivatePost(),
            post.isAchievement(),
            author.getDisplayName(),
            author.getUsername(),
            author.getRole() == null ? "STUDENT" : author.getRole().name(),
            author.getAvatarUrl(),
            post.getCreatedAt(),
            media
        );
    }

    private String normalizeMediaType(String raw) {
        if (raw == null) {
            return "IMAGE";
        }
        String upper = raw.trim().toUpperCase();
        if (upper.equals("VIDEO")) {
            return "VIDEO";
        }
        if (upper.equals("FILE")) {
            return "FILE";
        }
        return "IMAGE";
    }
}
