package com.fabiocondo.repository;

import com.fabiocondo.domain.Exam;
import com.fabiocondo.repository.query.ExamRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, Long>, ExamRepositoryQuery {
}
