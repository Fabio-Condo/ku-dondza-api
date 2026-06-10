package com.fabiocondo.tutor_ai.conversation;

import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.tutor_ai.message.TutorMessage;
import com.fabiocondo.tutor_ai.message.TutorMessageRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public TutorConversation save(TutorConversation conversation) {
        return conversationRepository.save(conversation);
    }

    public Page<TutorConversation> findByUser(Long userId, Pageable pageable) {
        return conversationRepository.findAllByUserIdOrderByUpdatedAtDesc(userId, pageable);
    }

    public TutorConversation findById(Long id) {
        return conversationRepository.findById(id).orElseThrow(() -> new RuntimeException("Conversa não encontrada"));
    }

    public TutorConversation getOrCreateForSubject(Long userId, Subject subject) {
        return conversationRepository.findByUserIdAndSubjectIdAndType(userId, subject.getId(), ConversationType.QUESTION)
                .orElseGet(() -> {
                    TutorConversation conversation = new TutorConversation();
                    conversation.setUserId(userId);
                    conversation.setSubject(subject);
                    conversation.setType(ConversationType.SUBJECT);
                    conversation.setCreatedAt(LocalDateTime.now());
                    conversation.setUpdatedAt(LocalDateTime.now());
                    return conversationRepository.save(conversation);
                });
    }

    public TutorConversation getOrCreateForTopic(Long userId, Topic topic) {
        // CORREÇÃO: usar o método correto com topic.getId()
        return conversationRepository.findByUserIdAndTopicIdAndType(userId, topic.getId(), ConversationType.TOPIC)
                .orElseGet(() -> {
                    TutorConversation conversation = new TutorConversation();
                    conversation.setUserId(userId);
                    conversation.setTopic(topic);
                    conversation.setType(ConversationType.TOPIC);
                    conversation.setCreatedAt(LocalDateTime.now());
                    conversation.setUpdatedAt(LocalDateTime.now());
                    return conversationRepository.save(conversation);
                });
    }

    public TutorConversation getOrCreateForQuestion(Long userId, Question question) {
        // CORREÇÃO: usar o método correto com question.getId()
        return conversationRepository.findByUserIdAndQuestionIdAndType(userId, question.getId(), ConversationType.QUESTION)
                .orElseGet(() -> {
                    TutorConversation conversation = new TutorConversation();
                    conversation.setUserId(userId);
                    conversation.setQuestion(question);
                    conversation.setType(ConversationType.QUESTION);
                    conversation.setCreatedAt(LocalDateTime.now());
                    conversation.setUpdatedAt(LocalDateTime.now());
                    return conversationRepository.save(conversation);
                });
    }

    public Page<TutorMessage> findMessagesByUserAndSubject(Long userId, Long subjectId, Pageable pageable) {
        Optional<TutorConversation> conversationOpt =
                conversationRepository.findByUserIdAndSubjectIdAndType(userId, subjectId, ConversationType.SUBJECT);

        if (!conversationOpt.isPresent()) {
            return new PageImpl<>(
                    Collections.emptyList(),
                    pageable,
                    0
            );
        }

        return messageRepository.findByConversationId(
                conversationOpt.get().getId(),
                pageable
        );
    }

    public Page<TutorMessage> findMessagesByUserAndQuestion(Long userId, Long questionId, Pageable pageable) {
        Optional<TutorConversation> conversationOpt =
                conversationRepository.findByUserIdAndQuestionIdAndType(userId, questionId, ConversationType.QUESTION);

        if (!conversationOpt.isPresent()) {
            return new PageImpl<>(
                    Collections.emptyList(),
                    pageable,
                    0
            );
        }

        return messageRepository.findByConversationId(
                conversationOpt.get().getId(),
                pageable
        );
    }

    public Page<TutorMessage> findMessagesByUserAndTopic(Long userId, Long topicId, Pageable pageable) {
        Optional<TutorConversation> conversationOpt =
                conversationRepository.findByUserIdAndTopicIdAndType(userId, topicId, ConversationType.TOPIC);

        if (!conversationOpt.isPresent()) {
            return new PageImpl<>(
                    Collections.emptyList(),
                    pageable,
                    0
            );
        }

        return messageRepository.findByConversationId(
                conversationOpt.get().getId(),
                pageable
        );
    }

    public List<TutorMessage> getLastSubjectMessages(Long userId, Long subjectId, int limit) {
        Pageable pageable = PageRequest.of(
                0,
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<TutorMessage> page = findMessagesByUserAndSubject(userId, subjectId, pageable);

        List<TutorMessage> messages = new ArrayList<>(page.getContent());
        Collections.reverse(messages);

        return messages;
    }

    public List<TutorMessage> getLastQuestionMessages(Long userId, Long questionId, int limit) {
        Pageable pageable = PageRequest.of(
                0,
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<TutorMessage> page = findMessagesByUserAndQuestion(userId, questionId, pageable);

        List<TutorMessage> messages = new ArrayList<>(page.getContent());
        Collections.reverse(messages);

        return messages;
    }

    public List<TutorMessage> getLastTopicMessages(Long userId, Long topicId, int limit) {
        Pageable pageable = PageRequest.of(
                0,
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<TutorMessage> page = findMessagesByUserAndTopic(userId, topicId, pageable);

        List<TutorMessage> messages = new ArrayList<>(page.getContent());
        Collections.reverse(messages);

        return messages;
    }
}