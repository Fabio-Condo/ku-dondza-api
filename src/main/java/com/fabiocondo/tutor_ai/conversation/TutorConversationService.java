package com.fabiocondo.tutor_ai.conversation;

import com.fabiocondo.domain.Question;
import com.fabiocondo.tutor_ai.message.TutorMessage;
import com.fabiocondo.tutor_ai.message.TutorMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TutorConversationService {

    private final TutorConversationRepository conversationRepository;
    private final TutorMessageRepository messageRepository;

    public TutorConversationService(TutorConversationRepository conversationRepository, TutorMessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public TutorConversation getOrCreate(Long userId, Question question) {

        return conversationRepository.findByUserIdAndQuestionId(userId, question.getId())
                .orElseGet(() -> {

                    TutorConversation c = new TutorConversation();

                    c.setUserId(userId);
                    c.setQuestion(question);
                    c.setCreatedAt(LocalDateTime.now());
                    c.setUpdatedAt(LocalDateTime.now());

                    return conversationRepository.save(c);
                });
    }

    public TutorConversation save(TutorConversation conversation) {
        return conversationRepository.save(conversation);
    }

    public Page<TutorConversation> findByUser(Long userId, Pageable pageable) {
        return conversationRepository.findAllByUserIdOrderByUpdatedAtDesc(userId, pageable);
    }

    public TutorConversation findById(Long id) {
        return conversationRepository.findById(id).orElseThrow(() -> new RuntimeException("Conversa não encontrada"));
    }

    public Page<TutorMessage> findMessagesByUserAndQuestion(
            Long userId,
            Long questionId,
            Pageable pageable) {

        Optional<TutorConversation> conversationOpt =
                conversationRepository.findByUserIdAndQuestionId(userId, questionId);

        if (!conversationOpt.isPresent()) {
            return new PageImpl<>(
                    Collections.emptyList(),
                    pageable,
                    0
            );
        }

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(
                conversationOpt.get().getId(),
                pageable
        );
    }
}