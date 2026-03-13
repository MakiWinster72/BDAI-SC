package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.ContactCreateRequest;
import com.gcsc.studentcenter.dto.ContactResponse;
import com.gcsc.studentcenter.dto.ContactUpdateRequest;
import com.gcsc.studentcenter.service.ContactEntryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactEntryController {

    private final ContactEntryService contactEntryService;

    public ContactEntryController(ContactEntryService contactEntryService) {
        this.contactEntryService = contactEntryService;
    }

    @GetMapping
    public ResponseEntity<List<ContactResponse>> list(
        Authentication authentication,
        @RequestParam(value = "all", required = false) Boolean all
    ) {
        boolean includeDisabled = Boolean.TRUE.equals(all);
        if (includeDisabled && !isManager(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(contactEntryService.list(includeDisabled));
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(
        Authentication authentication,
        @RequestBody ContactCreateRequest request
    ) {
        if (!isManager(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(contactEntryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> update(
        Authentication authentication,
        @PathVariable("id") Long id,
        @RequestBody ContactUpdateRequest request
    ) {
        if (!isManager(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(contactEntryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable("id") Long id) {
        if (!isManager(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        contactEntryService.delete(id);
        return ResponseEntity.ok().build();
    }

    private boolean isManager(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String value = authority.getAuthority();
            if ("ROLE_ADMIN".equals(value) || "ROLE_TEACHER".equals(value)) {
                return true;
            }
        }
        return false;
    }
}
