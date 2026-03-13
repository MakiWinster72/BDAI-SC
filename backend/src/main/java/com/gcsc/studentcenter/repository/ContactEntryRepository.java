package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.ContactEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactEntryRepository extends JpaRepository<ContactEntry, Long> {
    List<ContactEntry> findByActiveTrueOrderBySortOrderAscIdAsc();

    List<ContactEntry> findAllByOrderBySortOrderAscIdAsc();
}
