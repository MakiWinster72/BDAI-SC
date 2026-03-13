package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.ContactCreateRequest;
import com.gcsc.studentcenter.dto.ContactResponse;
import com.gcsc.studentcenter.dto.ContactUpdateRequest;
import com.gcsc.studentcenter.entity.ContactEntry;
import com.gcsc.studentcenter.repository.ContactEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactEntryService {

    private final ContactEntryRepository contactEntryRepository;

    public ContactEntryService(ContactEntryRepository contactEntryRepository) {
        this.contactEntryRepository = contactEntryRepository;
    }

    public List<ContactResponse> list(boolean includeDisabled) {
        List<ContactEntry> entries = includeDisabled
            ? contactEntryRepository.findAllByOrderBySortOrderAscIdAsc()
            : contactEntryRepository.findByActiveTrueOrderBySortOrderAscIdAsc();
        return entries.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ContactResponse create(ContactCreateRequest request) {
        ContactEntry entry = new ContactEntry();
        applyCreate(entry, request);
        LocalDateTime now = LocalDateTime.now();
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);
        ContactEntry saved = contactEntryRepository.save(entry);
        return toResponse(saved);
    }

    public ContactResponse update(Long id, ContactUpdateRequest request) {
        ContactEntry entry = contactEntryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("联系人不存在"));
        applyUpdate(entry, request);
        entry.setUpdatedAt(LocalDateTime.now());
        ContactEntry saved = contactEntryRepository.save(entry);
        return toResponse(saved);
    }

    public void delete(Long id) {
        ContactEntry entry = contactEntryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("联系人不存在"));
        contactEntryRepository.delete(entry);
    }

    private void applyCreate(ContactEntry entry, ContactCreateRequest request) {
        String name = normalize(request.getName());
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("名称不能为空");
        }
        entry.setName(name);
        entry.setOffice(normalize(request.getOffice()));
        entry.setDuty(normalize(request.getDuty()));
        entry.setPhone(normalize(request.getPhone()));
        entry.setPhotoUrl(normalize(request.getPhotoUrl()));
        entry.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        entry.setActive(request.getActive() == null || request.getActive());
    }

    private void applyUpdate(ContactEntry entry, ContactUpdateRequest request) {
        if (request.getName() != null) {
            String name = normalize(request.getName());
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("名称不能为空");
            }
            entry.setName(name);
        }
        if (request.getOffice() != null) {
            entry.setOffice(normalize(request.getOffice()));
        }
        if (request.getDuty() != null) {
            entry.setDuty(normalize(request.getDuty()));
        }
        if (request.getPhone() != null) {
            entry.setPhone(normalize(request.getPhone()));
        }
        if (request.getPhotoUrl() != null) {
            entry.setPhotoUrl(normalize(request.getPhotoUrl()));
        }
        if (request.getSortOrder() != null) {
            entry.setSortOrder(request.getSortOrder());
        }
        if (request.getActive() != null) {
            entry.setActive(request.getActive());
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private ContactResponse toResponse(ContactEntry entry) {
        return new ContactResponse(
            entry.getId(),
            entry.getName(),
            entry.getOffice(),
            entry.getDuty(),
            entry.getPhone(),
            entry.getPhotoUrl(),
            entry.getSortOrder(),
            entry.isActive(),
            entry.getCreatedAt(),
            entry.getUpdatedAt()
        );
    }
}
