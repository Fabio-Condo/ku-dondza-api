package com.fabiocondo.tutor_ai;

import com.fabiocondo.domain.Answer;
import com.fabiocondo.domain.MathExpression;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.GptService;
import com.fabiocondo.service.impl.QuestionService;
import com.fabiocondo.service.impl.UserServiceImpl;
import com.fabiocondo.tutor_ai.conversation.TutorConversation;
import com.fabiocondo.tutor_ai.conversation.TutorConversationService;
import com.fabiocondo.tutor_ai.message.TutorMessageService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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

    @Transactional
    public String ask(TutorRequest request) throws Exception {

        if (request == null) {
            throw new RuntimeException("Request inválido");
        }

        // ========================
        // USER VALIDATION
        // ========================
        User user = userService.findById(request.getUserId());

        if (user == null) {
            throw new UserNotFoundException(
                    "Usuário não encontrado pelo id: " + request.getUserId()
            );
        }

        // ========================
        // QUESTION VALIDATION
        // ========================
        Question question = questionService.findById(request.getQuestionId());

        if (question == null) {
            throw new QuestionNotFoundException(
                    "Questão não encontrada pelo id: " + request.getQuestionId()
            );
        }

        // ========================
        // OPTIONAL QUIZ CONTEXT
        // ========================
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

        // ========================
        // CONVERSATION
        // ========================
        TutorConversation conversation =
                conversationService.getOrCreate(
                        request.getUserId(),
                        question
                );

        // ========================
        // SAVE USER MESSAGE
        // ========================
        if (request.getMessage() != null &&
                !request.getMessage().trim().isEmpty()) {

            messageService.saveUserMessage(
                    conversation,
                    request.getMessage()
            );
        }

        // ========================
        // BUILD PROMPT
        // ========================
        String prompt = buildPrompt(
                question,
                selectedAnswer,
                correctAnswer,
                request.getMessage()
        );

        // ========================
        // CALL AI
        // ========================
        String aiResponse = gptService.askAssistant(prompt);

        // ========================
        // SAVE ASSISTANT MESSAGE
        // ========================
        messageService.saveAssistantMessage(
                conversation,
                aiResponse
        );

        // ========================
        // UPDATE CONVERSATION
        // ========================
        conversation.setUpdatedAt(
                java.time.LocalDateTime.now()
        );

        return aiResponse;
    }

    // =========================================================
    // PROMPT BUILDER
    // =========================================================
    private String buildPrompt(
            Question question,
            Answer selectedAnswer,
            Answer correctAnswer,
            String userMessage) {

        StringBuilder prompt = new StringBuilder();

        // ========================
        // TUTOR IDENTITY
        // ========================
        prompt.append("Você é um tutor inteligente da plataforma Dikahub.\n");
        prompt.append("Você ensina como um professor particular paciente.\n\n");

        // ========================
        // RULES
        // ========================
        prompt.append("REGRAS:\n");
        prompt.append("- Responda diretamente à dúvida do aluno.\n");
        prompt.append("- Seja claro, simples e didático.\n");
        prompt.append("- Não seja excessivamente longo.\n");
        prompt.append("- Explique erros e acertos quando houver contexto de quiz.\n");
        prompt.append("- Não assuma erro se não houver resposta selecionada.\n");
        prompt.append("- Use exemplos quando necessário.\n");
        prompt.append("- Se houver matemática, use LaTeX.\n");
        prompt.append("- Máximo recomendado: 12-15 linhas.\n\n");

        // ========================
        // QUESTION
        // ========================
        prompt.append("QUESTÃO:\n");
        prompt.append(question.getText()).append("\n\n");

        if (question.getTopic() != null) {
            prompt.append("TÓPICO: ")
                    .append(question.getTopic().getName())
                    .append("\n\n");
        }

        // ========================
        // OPTIONS
        // ========================
        prompt.append("ALTERNATIVAS:\n");
        for (Answer answer : question.getAnswers()) {
            prompt.append("- ").append(answer.getText()).append("\n");
        }
        prompt.append("\n");

        // ========================
        // MATH EXPRESSIONS
        // ========================
        if (question.getMathExpressions() != null &&
                !question.getMathExpressions().isEmpty()) {

            prompt.append("EXPRESSÕES MATEMÁTICAS:\n");

            for (MathExpression exp : question.getMathExpressions()) {

                prompt.append("- ");
                if (exp.getName() != null) {
                    prompt.append(exp.getName()).append(": ");
                }
                prompt.append(exp.getExpression()).append("\n");
            }

            prompt.append("\n");
        }

        // ========================
        // QUIZ MODE OR FREE MODE
        // ========================
        if (selectedAnswer != null) {

            prompt.append("CONTEXTO: QUIZ\n");
            prompt.append("O aluno respondeu a uma questão de múltipla escolha.\n\n");

            prompt.append("RESPOSTA DO ALUNO:\n");
            prompt.append(selectedAnswer.getText()).append("\n\n");

            if (correctAnswer != null) {
                prompt.append("RESPOSTA CORRETA:\n");
                prompt.append(correctAnswer.getText()).append("\n\n");
            }

            prompt.append("INSTRUÇÃO: Explique o erro ou confirme o acerto do aluno.\n\n");

        } else {

            prompt.append("CONTEXTO: TUTOR LIVRE\n");
            prompt.append("O aluno está a fazer uma pergunta sobre o conteúdo.\n\n");

            prompt.append("INSTRUÇÃO:\n");
            prompt.append("- Explique como um professor\n");
            prompt.append("- Não assuma erro\n");
            prompt.append("- Foque na compreensão do conceito\n\n");
        }

        // ========================
        // USER QUESTION
        // ========================
        prompt.append("PERGUNTA DO ALUNO:\n");
        prompt.append(userMessage != null ? userMessage : "").append("\n\n");

        // ========================
        // FINAL INSTRUCTIONS
        // ========================
        prompt.append("ESTRUTURA DA RESPOSTA:\n");
        prompt.append("1. Responda à dúvida do aluno.\n");
        prompt.append("2. Explique passo a passo.\n");
        prompt.append("3. Adapte ao contexto (quiz ou explicação).\n");
        prompt.append("4. Termine com uma dica simples.\n");

        return prompt.toString();
    }
}