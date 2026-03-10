package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.PostCreateRequest;
import com.gcsc.studentcenter.dto.PostResponse;
import com.gcsc.studentcenter.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(Authentication authentication, @RequestBody PostCreateRequest request) {
        String username = authentication.getName();
        return ResponseEntity.ok(postService.createPost(username, request));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> list(Authentication authentication,
                                                   @RequestParam(value = "type", required = false) String type) {
        String username = authentication.getName();
        return ResponseEntity.ok(postService.listPosts(username, type));
    }
}
