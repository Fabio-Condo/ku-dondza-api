package com.fabiocondo.tutor_ai.conversation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TutorConversationRepository extends JpaRepository<TutorConversation, Long> {

    Page<TutorConversation> findAllByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
    Optional<TutorConversation> findByUserIdAndTopicIdAndType(Long userId, Long topicId, ConversationType type);
    Optional<TutorConversation> findByUserIdAndQuestionIdAndType(Long userId, Long questionId, ConversationType type);


    // CORREÇÃO: usar @Query explícito para evitar confusão de tipos
    /*
    @Query("SELECT c FROM TutorConversation c WHERE c.userId = :userId AND c.topic.id = :topicId AND c.type = :type")
    Optional<TutorConversation> findByUserIdAndTopicIdAndType(
            @Param("userId") Long userId,
            @Param("topicId") Long topicId,
            @Param("type") ConversationType type
    );

    @Query("SELECT c FROM TutorConversation c WHERE c.userId = :userId AND c.question.id = :questionId AND c.type = :type")
    Optional<TutorConversation> findByUserIdAndQuestionIdAndType(
            @Param("userId") Long userId,
            @Param("questionId") Long questionId,
            @Param("type") ConversationType type
    );
    */
}