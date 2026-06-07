package com.fabiocondo.tutor_ai.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TutorMessageRepository extends JpaRepository<TutorMessage, Long> {
     Page<TutorMessage> findByConversationId(Long conversationId, Pageable pageable);

    Page<TutorMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);
}
