package com.fabiocondo.tutor_ai;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.GptService;
import com.fabiocondo.service.impl.QuestionService;
import com.fabiocondo.service.impl.UserServiceImpl;
import com.fabiocondo.tutor_ai.conversation.TutorConversation;
import com.fabiocondo.tutor_ai.conversation.TutorConversationService;
import com.fabiocondo.tutor_ai.message.MessageRole;
import com.fabiocondo.tutor_ai.message.TutorMessage;
import com.fabiocondo.tutor_ai.message.TutorMessageService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TutorAiService {

    private final QuestionService questionService;
    private final GptService gptService;
    private final TutorConversationService conversationService;
    private final TutorMessageService messageService;
    private final UserServiceImpl userService;

    public TutorAiService(
            QuestionService questionService,
            GptService gptService,
            TutorConversationService conversationService,
            TutorMessageService messageService,
            UserServiceImpl userService) {

        this.questionService = questionService;
        this.gptService = gptService;
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.userService = userService;
    }

    // =========================================================
    // MAIN METHOD
    // =========================================================
    @Transactional
    public String ask(TutorRequest request) throws Exception {

        if (request == null) {
            throw new RuntimeException("Request inválido");
        }

        User user = userService.findById(request.getUserId());
        if (user == null) throw new UserNotFoundException("User não encontrado");

        Question question = questionService.findById(request.getQuestionId());
        if (question == null) throw new QuestionNotFoundException("Questão não encontrada");

        TutorConversation conversation =
                conversationService.getOrCreate(request.getUserId(), question);

        // =========================
        // INTENT
        // =========================
        TutorIntent intent = detectIntent(request.getMessage());

        // =========================
        // SMALL TALK (CORRIGIDO)
        // =========================
        if (intent == TutorIntent.SMALL_TALK) {

            String response = "De nada 👍 Se quiser continuar, posso te ajudar com mais exercícios.";

            saveMessage(conversation, request.getMessage(), response);

            return response;
        }

        Answer selectedAnswer = null;

        if (request.getSelectedAnswerId() != null) {
            selectedAnswer = question.getAnswers()
                    .stream()
                    .filter(a -> a.getId().equals(request.getSelectedAnswerId()))
                    .findFirst()
                    .orElse(null);
        }

        Answer correctAnswer = question.getAnswers()
                .stream()
                .filter(Answer::isCorrect)
                .findFirst()
                .orElse(null);

        List<TutorMessage> history =
                conversationService.getLastMessages(
                        request.getUserId(),
                        request.getQuestionId(),
                        4 // 🔥 reduzido (importante)
                );

        // =========================
        // BUILD PROMPT
        // =========================
        String prompt = buildPrompt(
                question,
                selectedAnswer,
                correctAnswer,
                request.getMessage(),
                history,
                intent
        );

        String response = gptService.askAssistant(prompt);

        saveMessage(conversation, request.getMessage(), response);

        conversation.setUpdatedAt(LocalDateTime.now());

        return response;
    }

    // =========================================================
    // SAVE MESSAGE
    // =========================================================
    private void saveMessage(TutorConversation conversation, String userMsg, String aiMsg) {

        if (userMsg != null && !userMsg.trim().isEmpty()) {
            messageService.saveUserMessage(conversation, userMsg);
        }

        messageService.saveAssistantMessage(conversation, aiMsg);
    }

    // =========================================================
    // INTENT DETECTOR (FIXED)
    // =========================================================
    private TutorIntent detectIntent(String message) {

        if (message == null) return TutorIntent.HELP;

        String msg = message.toLowerCase().trim();

        // 🔥 SMALL TALK (IMPORTANTE)
        if (msg.equals("ok") ||
                msg.equals("okey") ||
                msg.equals("thanks") ||
                msg.equals("thank you") ||
                msg.equals("obrigado") ||
                msg.equals("obrigada") ||
                msg.equals("valeu") ||
                msg.equals("entendi") ||
                msg.equals("percebi") ||
                msg.equals("👍")) {

            return TutorIntent.SMALL_TALK;
        }

        if (msg.contains("não entendi") ||
                msg.contains("como faz") ||
                msg.contains("explica") ||
                msg.contains("ajuda")) {

            return TutorIntent.HELP;
        }

        // 🔥 IMPORTANTE: se chegou aqui e tem resposta selecionada
        return TutorIntent.HELP;
    }

    // =========================================================
    // PROMPT BUILDER (ADAPTATIVO)
    // =========================================================
    private String buildPrompt(
            Question question,
            Answer selectedAnswer,
            Answer correctAnswer,
            String userMessage,
            List<TutorMessage> history,
            TutorIntent intent) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("Você é um tutor de matemática.\n");
        prompt.append("Seja claro e pedagógico.\n\n");

        // =========================
        // CONTEXT SWITCH
        // =========================
        if (selectedAnswer == null) {

            prompt.append("CONTEXTO: AJUDA\n");
            prompt.append("Ajude o aluno a pensar, não dê resposta direta.\n\n");

        } else {

            prompt.append("CONTEXTO: CORREÇÃO\n");
            prompt.append("Explique o erro ou acerto de forma breve.\n\n");

            prompt.append("Resposta aluno: ")
                    .append(selectedAnswer.getText())
                    .append("\n");

            if (correctAnswer != null) {
                prompt.append("Correta: ")
                        .append(correctAnswer.getText())
                        .append("\n\n");
            }
        }

        prompt.append("QUESTÃO:\n")
                .append(question.getText())
                .append("\n\n");

        // =========================
        // HISTORY (REDUZIDO)
        // =========================
        if (history != null && !history.isEmpty()) {

            prompt.append("HISTÓRICO:\n");

            for (TutorMessage m : history) {

                String role = m.getRole() == MessageRole.USER ? "ALUNO" : "TUTOR";

                String content = m.getContent();
                if (content.length() > 200) {
                    content = content.substring(0, 200) + "...";
                }

                prompt.append(role).append(": ").append(content).append("\n");
            }

            prompt.append("\n");
        }

        prompt.append("PERGUNTA DO ALUNO:\n")
                .append(userMessage)
                .append("\n\n");

        // =========================
        // FORMAT RULES ADAPTIVAS
        // =========================
        if (intent == TutorIntent.HELP) {

            prompt.append("FORMATO:\n");
            prompt.append("1. Explicação\n2. Passo a passo\n3. Exemplo\n4. Dica\n");

        } else {

            prompt.append("FORMATO:\n");
            prompt.append("Responda de forma curta e direta.\n");
        }

        return prompt.toString();
    }

    // =========================================================
    // ENUM
    // =========================================================
    public enum TutorIntent {
        HELP,
        SMALL_TALK
    }
}