package com.fabiocondo.tutor_ai.conversation;

import com.fabiocondo.tutor_ai.message.TutorMessage;
import com.fabiocondo.tutor_ai.message.TutorMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class TutorConversationController {

    private final TutorConversationService conversationService;

    public TutorConversationController(TutorConversationService conversationService, TutorMessageService messageService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/{userId}/all")
    public ResponseEntity<Page<TutorConversation>> findConversationsByUser(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(conversationService.findByUser(userId, pageable));
    }

    @GetMapping("/{id}/messages/question")
    public Page<TutorMessageResponse> getQuestionConversationsMessages(
            @RequestParam Long userId,
            @RequestParam Long questionId,
            Pageable pageable) {

        return conversationService
                .findMessagesByUserAndQuestion(userId, questionId, pageable)
                .map(this::toResponse);
    }

    @GetMapping("/{id}/messages/topic")
    public Page<TutorMessageResponse> getTopicConversationsMessages(
            @RequestParam Long userId,
            @RequestParam Long topicId,
            Pageable pageable) {

        return conversationService
                .findMessagesByUserAndTopic(userId, topicId, pageable)
                .map(this::toResponse);
    }

    @GetMapping("/{id}/messages/subject")
    public Page<TutorMessageResponse> getSubjectConversationsMessages(
            @RequestParam Long userId,
            @RequestParam Long subjectId,
            Pageable pageable) {

        return conversationService
                .findMessagesByUserAndSubject(userId, subjectId, pageable)
                .map(this::toResponse);
    }

    public TutorMessageResponse toResponse(TutorMessage message) {

        TutorMessageResponse dto = new TutorMessageResponse();

        dto.setId(message.getId());
        dto.setRole(message.getRole().name());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());

        return dto;
    }
}
