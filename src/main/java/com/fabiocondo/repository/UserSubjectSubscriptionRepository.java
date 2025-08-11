package com.fabiocondo.repository;

import com.fabiocondo.domain.UserSubjectSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSubjectSubscriptionRepository extends JpaRepository<UserSubjectSubscription, Long> {
    Optional<UserSubjectSubscription> findByUserIdAndSubjectId(Long userId, Long subjectId);
    Page<UserSubjectSubscription> findByUserId(Long userId, Pageable pageable);
    Page<UserSubjectSubscription> findBySubjectId(Long subjectId, Pageable pageable);
    boolean existsByUserIdAndSubjectId(Long userId, Long subjectId);
}
