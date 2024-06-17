package com.fabiocondo.repository;

import com.fabiocondo.domain.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherRepository  extends JpaRepository<Teacher, Long> {

    @Query("SELECT t FROM Teacher t WHERE t.name LIKE %:name%")
    public Page<Teacher> findByName(@Param("name") String name, Pageable pageable);
}
