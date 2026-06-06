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

    @GetMapping("/{id}/messages")
    public Page<TutorMessageResponse> getConversationsMessages(
            @RequestParam Long userId,
            @RequestParam Long questionId,
            Pageable pageable) {

        return conversationService
                .findMessagesByUserAndQuestion(userId, questionId, pageable)
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
