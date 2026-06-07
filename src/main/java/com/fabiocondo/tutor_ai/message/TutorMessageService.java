package com.fabiocondo.tutor_ai.message;

import com.fabiocondo.tutor_ai.conversation.TutorConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class TutorMessageService {

    private final TutorMessageRepository repository;

    public TutorMessageService(
            TutorMessageRepository repository) {

        this.repository = repository;
    }

    public TutorMessage saveUserMessage(TutorConversation conversation, String content) {

        TutorMessage message = new TutorMessage();

        message.setConversation(conversation);
        message.setRole(MessageRole.USER);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());

        return repository.save(message);
    }

    public TutorMessage saveAssistantMessage(TutorConversation conversation, String content) {

        TutorMessage message = new TutorMessage();

        message.setConversation(conversation);
        message.setRole(MessageRole.ASSISTANT);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());

        return repository.save(message);
    }

    //public List<TutorMessage> findConversationMessages(Long conversationId) {
    //    return repository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    //}

    //public Page<TutorMessage> findConversationMessages(Long conversationId, Pageable pageable) {
    //    return repository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);
    //}
}
