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
import com.fabiocondo.tutor_ai.message.MessageRole;
import com.fabiocondo.tutor_ai.message.TutorMessage;
import com.fabiocondo.tutor_ai.message.TutorMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TutorAiService {

    private static final Logger log = LoggerFactory.getLogger(TutorAiService.class);

    private final QuestionService questionService;
    private final GptService gptService;
    private final TutorConversationService conversationService;
    private final TutorMessageService messageService;
    private final UserServiceImpl userService;

    // Pattern para detectar mensagens muito curtas ou sem sentido
    private static final Pattern GIBBERISH_PATTERN = Pattern.compile(
            "^(?i)(asdf|qwerty|zxcv|teste?|kkk|rsrs|h{2,}|[?]{2,}|[!]{2,}|[.]{3,})$"
    );

    private static final Pattern VERY_SHORT_PATTERN = Pattern.compile("^.{1,2}$");

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
    // HELPER: Get first name
    // =========================================================
    private String getFirstName(User user) {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return "aluno";
        }
        String fullName = user.getFullName().trim();
        int spaceIndex = fullName.indexOf(' ');
        return spaceIndex > 0 ? fullName.substring(0, spaceIndex) : fullName;
    }

    // =========================================================
    // SIMPLE INTENT DETECTION (rule-based, mais estável)
    // =========================================================
    private TutorIntent detectIntentSimple(String message) {
        if (message == null || message.trim().isEmpty()) {
            return TutorIntent.HINT;
        }

        String msg = message.toLowerCase().trim();

        // Detectar mensagens confusas primeiro
        if (VERY_SHORT_PATTERN.matcher(msg).matches()) {
            return TutorIntent.UNCLEAR;
        }

        if (GIBBERISH_PATTERN.matcher(msg).matches()) {
            return TutorIntent.UNCLEAR;
        }

        // Greetings
        if (msg.matches("^(oi|ol[aá]|bom dia|boa tarde|boa noite|hey|hi|e aí|opa|fala|beleza|td bem|tudo bem|salve).*")) {
            return TutorIntent.GREETING;
        }

        // Thanks
        if (msg.matches("^(obrigado|obrigada|valeu|agradeço|muito obrigado|brigado|brigada|vlw).*")) {
            return TutorIntent.THANKS;
        }

        // Praise
        if (msg.matches(".*(você é (ótimo|bom|excelente|incrível)|gostei (da explicação|da aula)|bom tutor|muito bom).*")) {
            return TutorIntent.PRAISE;
        }

        // Step by step
        if (msg.contains("passo a passo") || msg.contains("resolver comigo") || msg.contains("guia") || msg.contains("me orienta")) {
            return TutorIntent.STEP_BY_STEP;
        }

        // Verify reasoning
        if (msg.contains("acho que") || msg.contains("meu raciocínio") || msg.contains("está certo") || msg.contains("correto")) {
            return TutorIntent.VERIFY_REASONING;
        }

        // Hint
        if (msg.contains("dica") || msg.contains("ajuda") || msg.contains("como começo") || msg.contains("por onde começar")) {
            return TutorIntent.HINT;
        }

        // Out of scope (palavras comuns fora do contexto educacional)
        if (msg.matches(".*(clima|tempo|futebol|notícias|política|preço|dinheiro|comprar|vender).*") &&
                !msg.matches(".*(questão|exercício|prova|estudo|matéria|aula|conteúdo).*")) {
            return TutorIntent.OUT_OF_SCOPE;
        }

        // Default para explicação
        return TutorIntent.EXPLANATION;
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
        if (user == null) {
            throw new UserNotFoundException("Usuário não encontrado");
        }

        Question question = questionService.findById(request.getQuestionId());
        if (question == null) {
            throw new QuestionNotFoundException("Questão não encontrada");
        }

        TutorConversation conversation =
                conversationService.getOrCreate(request.getUserId(), question);

        // Detect intent (usando regras simples, sem chamar IA)
        TutorIntent intent = detectIntentSimple(request.getMessage());
        log.info("Usuário {} - Questão {} - Intent detectada: {}",
                request.getUserId(), request.getQuestionId(), intent);

        // =====================================================
        // UNCLEAR MESSAGE (rápido, sem chamar GPT)
        // =====================================================
        if (intent == TutorIntent.UNCLEAR) {
            String response = buildUnclearResponse(getFirstName(user));
            messageService.saveUserMessage(conversation, request.getMessage());
            messageService.saveAssistantMessage(conversation, response);
            conversation.setUpdatedAt(LocalDateTime.now());
            return response;
        }

        // =====================================================
        // SOCIAL INTERACTIONS (respostas rápidas, sem GPT)
        // =====================================================
        if (intent == TutorIntent.GREETING || intent == TutorIntent.THANKS || intent == TutorIntent.PRAISE) {
            String response = buildSocialResponse(intent, getFirstName(user));
            messageService.saveUserMessage(conversation, request.getMessage());
            messageService.saveAssistantMessage(conversation, response);
            conversation.setUpdatedAt(LocalDateTime.now());
            log.info("Resposta social para usuário {}", request.getUserId());
            return response;
        }

        // =====================================================
        // OUT OF SCOPE (resposta rápida, sem GPT)
        // =====================================================
        if (intent == TutorIntent.OUT_OF_SCOPE) {
            String subject = question.getTopic().getSubject().getName();
            String response = String.format(
                    "Olá %s! Posso ajudar apenas com dúvidas relacionadas a esta questão de %s. Vamos focar no assunto? 😊",
                    getFirstName(user),
                    subject != null ? subject : "esta disciplina"
            );
            messageService.saveUserMessage(conversation, request.getMessage());
            messageService.saveAssistantMessage(conversation, response);
            conversation.setUpdatedAt(LocalDateTime.now());
            return response;
        }

        // =====================================================
        // PROCESSAMENTO NORMAL (chama GPT)
        // =====================================================
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

        List<TutorMessage> history = conversationService.getLastMessages(
                request.getUserId(),
                request.getQuestionId(),
                8  // Reduzido de 12 para 8 para economizar tokens
        );

        if (request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
            messageService.saveUserMessage(conversation, request.getMessage());
        }

        String prompt = buildPrompt(
                question,
                selectedAnswer,
                correctAnswer,
                request.getMessage(),
                history,
                intent,
                user
        );

        if (log.isDebugEnabled()) {
            log.debug("Prompt enviado ao GPT: {}", prompt.substring(0, Math.min(500, prompt.length())));
        }

        String aiResponse = gptService.askAssistant(prompt);
        messageService.saveAssistantMessage(conversation, aiResponse);
        conversation.setUpdatedAt(LocalDateTime.now());

        return aiResponse;
    }

    // =========================================================
    // UNCLEAR RESPONSE BUILDER
    // =========================================================
    private String buildUnclearResponse(String firstName) {
        String[] responses = {
                String.format("Desculpe, %s, não consegui entender. Pode reformular sua pergunta? 🤔", firstName),
                String.format("%s, sua pergunta ficou um pouco confusa. Você poderia explicar melhor? 😊", firstName),
                String.format("Não entendi completamente, %s. Pode dar mais detalhes? 📝", firstName),
                String.format("%s, não ficou claro o que você precisa. Pode me dizer com mais detalhes? 🎓", firstName),
        };
        return responses[(int) (Math.random() * responses.length)];
    }

    // =========================================================
    // SOCIAL RESPONSE BUILDER
    // =========================================================
    private String buildSocialResponse(TutorIntent intent, String firstName) {
        switch (intent) {
            case GREETING:
                return String.format("Olá %s! Como posso ajudar você com os estudos hoje? 😊", firstName);
            case THANKS:
                String[] thanks = {
                        String.format("Por nada, %s! Estou aqui para ajudar. Mais alguma dúvida? 📚", firstName),
                        String.format("Disponha, %s! Precisa de ajuda com mais alguma coisa? 🎓", firstName),
                        String.format("Fico feliz em ajudar, %s! Continue estudando! 💪", firstName),
                        String.format("Imagina, %s! Vamos em frente! 🚀", firstName)
                };
                return thanks[(int) (Math.random() * thanks.length)];
            case PRAISE:
                String[] praise = {
                        String.format("Obrigado, %s! Fico feliz que está gostando. Vamos continuar? 😊", firstName),
                        String.format("Que legal, %s! Posso ajudar em algo mais? 🌟", firstName),
                        String.format("Valeu, %s! Tem mais alguma dúvida? 📖", firstName),
                };
                return praise[(int) (Math.random() * praise.length)];
            default:
                return String.format("Olá %s! Como posso ajudar? 😊", firstName);
        }
    }

    // =========================================================
    // PROMPT BUILDER
    // =========================================================
    private String buildPrompt(
            Question question,
            Answer selectedAnswer,
            Answer correctAnswer,
            String userMessage,
            List<TutorMessage> history,
            TutorIntent intent,
            User user) {

        StringBuilder prompt = new StringBuilder();
        String subject = question.getTopic().getSubject().getName();
        String firstName = getFirstName(user);

        prompt.append("Você é o Tutor AI da plataforma Dikahub.\n");
        prompt.append("Está ajudando ").append(firstName).append(" com uma questão de ").append(subject).append(".\n\n");

        prompt.append("REGRAS:\n");
        prompt.append("- Seja pedagógico e acolhedor\n");
        prompt.append("- Use o nome ").append(firstName).append(" na conversa\n");
        prompt.append("- Não dê a resposta pronta\n");
        prompt.append("- Estimule o raciocínio\n\n");

        prompt.append("TIPO DE AJUDA:\n");
        switch (intent) {
            case HINT:
                prompt.append("Dê apenas uma dica curta.\n\n");
                break;
            case STEP_BY_STEP:
                prompt.append("Guie o aluno passo a passo.\n\n");
                break;
            case VERIFY_REASONING:
                prompt.append("Analise o raciocínio do aluno.\n\n");
                break;
            default:
                prompt.append("Explique o conceito necessário.\n\n");
        }

        prompt.append("QUESTÃO:\n").append(question.getText()).append("\n\n");

        prompt.append("ALTERNATIVAS:\n");
        for (Answer answer : question.getAnswers()) {
            prompt.append("- ").append(answer.getText()).append("\n");
        }
        prompt.append("\n");

        if (selectedAnswer != null) {
            prompt.append("RESPOSTA DO ALUNO: ").append(selectedAnswer.getText()).append("\n\n");
            if (intent == TutorIntent.VERIFY_REASONING && correctAnswer != null) {
                prompt.append("(Referência - resposta correta: ").append(correctAnswer.getText()).append(")\n");
                prompt.append("NÃO revele esta resposta ao aluno.\n\n");
            }
        }

        if (!history.isEmpty()) {
            prompt.append("HISTÓRICO:\n");
            for (TutorMessage msg : history.subList(Math.max(0, history.size() - 6), history.size())) {
                String role = msg.getRole() == MessageRole.USER ? firstName : "TUTOR";
                prompt.append(role).append(": ").append(msg.getContent()).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("PERGUNTA: ").append(userMessage).append("\n\n");
        prompt.append("Responda de forma educada e didática, sempre estimulando o raciocínio do aluno.\n");

        return prompt.toString();
    }

    // =========================================================
    // INTENT ENUM
    // =========================================================
    public enum TutorIntent {
        GREETING,
        THANKS,
        PRAISE,
        HINT,
        EXPLANATION,
        STEP_BY_STEP,
        VERIFY_REASONING,
        OUT_OF_SCOPE,
        UNCLEAR
    }
}