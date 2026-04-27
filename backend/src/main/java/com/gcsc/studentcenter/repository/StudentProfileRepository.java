package com.gcsc.studentcenter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gcsc.studentcenter.dto.StudentSearchItemResponse;
import com.gcsc.studentcenter.entity.StudentProfile;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);

    @Query("""
        select new com.gcsc.studentcenter.dto.StudentSearchItemResponse(
            sp.id,
            coalesce(sp.fullName, u.displayName),
            coalesce(sp.studentNo, u.studentNo),
            sp.classYear,
            sp.classMajor,
            sp.classNo,
            sp.college,
            sp.hkMoTw,
            sp.specialStudent,
            sp.specialStudentType
        )
        from StudentProfile sp
        join sp.user u
        where u.role in (com.gcsc.studentcenter.entity.UserRole.STUDENT, com.gcsc.studentcenter.entity.UserRole.CADRE)
            and (:classYear is null or sp.classYear = :classYear)
            and (:classNo is null or sp.classNo = :classNo)
            and (:college is null or sp.college = :college)
            and (:major is null or sp.classMajor = :major)
            and (:hkMoTw is null or sp.hkMoTw = :hkMoTw)
            and (:specialStudent is null or sp.specialStudent = :specialStudent)
            and (:specialStudentType is null or sp.specialStudentType = :specialStudentType)
            and (:studentCategory is null or sp.studentCategory = :studentCategory)
            and (:allowedClassNames is null or sp.className in :allowedClassNames)
            and (
                :keyword is null
                or lower(sp.fullName) like lower(concat('%', :keyword, '%'))
                or lower(u.displayName) like lower(concat('%', :keyword, '%'))
                or lower(u.username) like lower(concat('%', :keyword, '%'))
                or lower(sp.className) like lower(concat('%', :keyword, '%'))
                or lower(sp.college) like lower(concat('%', :keyword, '%'))
                or lower(sp.classMajor) like lower(concat('%', :keyword, '%'))
                or lower(sp.classNo) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(sp.studentNo, u.studentNo)) like lower(concat('%', :keyword, '%'))
            )
        """)
    Page<StudentSearchItemResponse> searchProfiles(
        @Param("classYear") Integer classYear,
        @Param("classNo") String classNo,
        @Param("college") String college,
        @Param("major") String major,
        @Param("hkMoTw") Boolean hkMoTw,
        @Param("specialStudent") Boolean specialStudent,
        @Param("specialStudentType") String specialStudentType,
        @Param("studentCategory") String studentCategory,
        @Param("keyword") String keyword,
        @Param("allowedClassNames") List<String> allowedClassNames,
        Pageable pageable
    );
}
