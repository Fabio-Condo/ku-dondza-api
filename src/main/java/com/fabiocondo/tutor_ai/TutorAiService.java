package com.fabiocondo.tutor_ai;

import com.fabiocondo.domain.Answer;
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
import java.text.Normalizer;
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
    // HELPER: Normalize text (remove accents, lowercase)
    // =========================================================
    private String normalizeText(String text) {
        if (text == null) return "";
        // Remove acentos
        String normalized = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        // Remove pontuação extra, mas mantém essencial
        normalized = normalized.replaceAll("[?¿!¡;:,.()\\[\\]{}<>]", " ");
        // Remove espaços extras
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    // =========================================================
    // HELPER: Check if message contains word with variations
    // =========================================================
    private boolean containsWord(String message, String... words) {
        String normalizedMsg = normalizeText(message);
        for (String word : words) {
            String normalizedWord = normalizeText(word);
            // Verifica como palavra completa
            if (normalizedMsg.matches(".*\\b" + Pattern.quote(normalizedWord) + "\\b.*")) {
                return true;
            }
        }
        return false;
    }

    // =========================================================
    // HELPER: Check if message matches pattern with variations
    // =========================================================
    private boolean matchesPattern(String message, String pattern) {
        String normalizedMsg = normalizeText(message);
        return normalizedMsg.matches(pattern);
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
    // HELPER: Get topic name
    // =========================================================
    private String getTopicName(Question question) {
        if (question.getTopic() != null && question.getTopic().getName() != null) {
            return question.getTopic().getName();
        }
        return "este tópico";
    }

    // =========================================================
    // SIMPLE INTENT DETECTION (rule-based, mais estável e tolerante)
    // =========================================================
    private TutorIntent detectIntentSimple(String message) {
        if (message == null || message.trim().isEmpty()) {
            return TutorIntent.HINT;
        }

        String rawMsg = message.trim();

        // Detectar mensagens confusas primeiro (usando texto original)
        if (VERY_SHORT_PATTERN.matcher(rawMsg).matches()) {
            return TutorIntent.UNCLEAR;
        }

        if (GIBBERISH_PATTERN.matcher(rawMsg).matches()) {
            return TutorIntent.UNCLEAR;
        }

        // Usar texto normalizado para as comparações
        String msg = normalizeText(rawMsg);

        // How is the tutor? (perguntas sobre o estado do tutor - com variações)
        if (msg.matches(".*(como (esta|vc esta|voce esta|ta)|tudo bem|beleza|como vai|como anda|como estao as coisas|como funciona|como voce esta).*") ||
                containsWord(rawMsg, "como está", "como esta", "como voce esta", "como você está", "tudo bem", "beleza")) {
            return TutorIntent.HOW_ARE_YOU;
        }

        // Greetings (com variações de escrita)
        if (msg.matches("^(oi|ola|bom dia|boa tarde|boa noite|hey|hi|e ai|opa|fala|beleza|td bem|tudo bem|salve|iae|iae beleza).*") ||
                containsWord(rawMsg, "oi", "olá", "ola", "bom dia", "boa tarde", "boa noite", "e aí", "e ai")) {
            return TutorIntent.GREETING;
        }

        // Thanks (com variações)
        if (containsWord(rawMsg, "obrigado", "obrigada", "valeu", "agradeço", "muito obrigado", "brigado", "brigada", "vlw", "obg", "obgd")) {
            return TutorIntent.THANKS;
        }

        // Praise (com variações)
        if (containsWord(rawMsg, "você é ótimo", "voce é otimo", "bom tutor", "muito bom", "excelente", "incrível", "incrivel", "gostei da explicação", "gostei da explicacao")) {
            return TutorIntent.PRAISE;
        }

        // Step by step (com variações)
        if (containsWord(rawMsg, "passo a passo", "resolver comigo", "me guia", "me orienta", "passo a passo", "me ajuda a resolver")) {
            return TutorIntent.STEP_BY_STEP;
        }

        // Verify reasoning (com variações)
        if (containsWord(rawMsg, "acho que", "meu raciocínio", "meu raciocinio", "está certo", "esta certo", "correto", "fiz certo", "esta correto")) {
            return TutorIntent.VERIFY_REASONING;
        }

        // Hint (com variações)
        if (containsWord(rawMsg, "dica", "ajuda", "como começo", "como comeco", "por onde começar", "por onde comecar", "me ajuda")) {
            return TutorIntent.HINT;
        }

        // Out of scope (palavras comuns fora do contexto educacional)
        if (containsWord(rawMsg, "clima", "tempo", "futebol", "notícias", "noticias", "política", "politica", "preço", "preco", "dinheiro", "comprar", "vender", "filme", "serie", "música", "musica", "jogo", "viagem", "fim de semana", "feriado")) {
            // Verifica se também não tem palavras do contexto educacional
            if (!containsWord(rawMsg, "questão", "questao", "exercício", "exercicio", "prova", "estudo", "matéria", "materia", "aula", "conteúdo", "conteudo")) {
                return TutorIntent.OUT_OF_SCOPE;
            }
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
        String topicName = getTopicName(question);

        log.info("Usuário {} - Questão {} (Tópico: {}) - Intent detectada: {}",
                request.getUserId(), request.getQuestionId(), topicName, intent);

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
        // HOW ARE YOU (perguntas sobre o estado do tutor)
        // =====================================================
        if (intent == TutorIntent.HOW_ARE_YOU) {
            String response = buildHowAreYouResponse(getFirstName(user));
            messageService.saveUserMessage(conversation, request.getMessage());
            messageService.saveAssistantMessage(conversation, response);
            conversation.setUpdatedAt(LocalDateTime.now());
            log.info("How are you response para usuário {}", request.getUserId());
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
        // OUT OF SCOPE (resposta rápida e amigável, sem GPT)
        // =====================================================
        if (intent == TutorIntent.OUT_OF_SCOPE) {
            String response = buildOutOfScopeResponse(getFirstName(user), topicName);
            messageService.saveUserMessage(conversation, request.getMessage());
            messageService.saveAssistantMessage(conversation, response);
            conversation.setUpdatedAt(LocalDateTime.now());
            log.info("Out of scope detectado para usuário {}", request.getUserId());
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
                8
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
                user,
                topicName
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
    // OUT OF SCOPE RESPONSE BUILDER
    // =========================================================
    private String buildOutOfScopeResponse(String firstName, String topicName) {
        String[] responses = {
                String.format("Olá %s! Posso ajudar apenas com dúvidas relacionadas a **%s**. Vamos focar neste tópico? 😊", firstName, topicName),
                String.format("%s, minha especialidade é ajudar com **%s**. Que tal voltarmos para a questão? 📚", firstName, topicName),
                String.format("Entendo sua curiosidade, %s, mas sou especializado em **%s**. Posso ajudar com isso? 🎯", firstName, topicName),
                String.format("Essa é uma pergunta interessante, %s! Porém, meu foco é auxiliar em **%s**. Vamos continuar com a questão? 💪", firstName, topicName),
                String.format("Sinto muito, %s, mas só posso ajudar com **%s**. Tem alguma dúvida sobre este tópico? 🤔", firstName, topicName)
        };
        return responses[(int) (Math.random() * responses.length)];
    }

    // =========================================================
    // HOW ARE YOU RESPONSE BUILDER
    // =========================================================
    private String buildHowAreYouResponse(String firstName) {
        String[] responses = {
                String.format("Estou muito bem, %s! Obrigado por perguntar. Pronto para ajudar você com os estudos? 😊", firstName),
                String.format("Tudo ótimo, %s! Estou aqui funcionando perfeitamente. Qual sua dúvida hoje? 🚀", firstName),
                String.format("Estou bem, sim! E você, %s? Preparado para resolver mais essa questão? 💪", firstName),
                String.format("Tudo certo por aqui, %s! Ansioso para ajudar você. Qual a dificuldade que está enfrentando? 📚", firstName),
                String.format("Estou ótimo, %s! Obrigado por perguntar. Vamos focar na questão? 🎯", firstName)
        };
        return responses[(int) (Math.random() * responses.length)];
    }

    // =========================================================
    // UNCLEAR RESPONSE BUILDER
    // =========================================================
    private String buildUnclearResponse(String firstName) {
        String[] responses = {
                String.format("Desculpe, %s, não consegui entender. Pode reformular sua pergunta ou resposta? 🤔", firstName),
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
                String[] greetings = {
                        String.format("Olá %s! Como posso ajudar você com os estudos hoje? 😊", firstName),
                        String.format("Oi %s! Estou aqui para ajudar. Qual sua dúvida? 📚", firstName),
                        String.format("Fala, %s! Vamos resolver essa questão? 💪", firstName)
                };
                return greetings[(int) (Math.random() * greetings.length)];
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
            User user,
            String topicName) {

        StringBuilder prompt = new StringBuilder();
        String firstName = getFirstName(user);

        prompt.append("Você é o Tutor AI da plataforma Dikahub.\n");
        prompt.append("Está ajudando ").append(firstName).append(" com uma questão sobre **").append(topicName).append("**.\n\n");

        prompt.append("REGRAS:\n");
        prompt.append("- Seja pedagógico e acolhedor\n");
        prompt.append("- Use o nome ").append(firstName).append(" na conversa\n");
        prompt.append("- Não dê a resposta pronta\n");
        prompt.append("- Estimule o raciocínio\n");
        prompt.append("- Se o aluno perguntar algo fora do tópico, redirecione educadamente de volta para **").append(topicName).append("**\n\n");

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

        prompt.append("TÓPICO: ").append(topicName).append("\n\n");
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
        HOW_ARE_YOU,
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