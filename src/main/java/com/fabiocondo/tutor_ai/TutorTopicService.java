package com.fabiocondo.tutor_ai;

import com.fabiocondo.domain.Topic;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.GptService;
import com.fabiocondo.service.impl.TopicService;
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

@Service
public class TutorTopicService {

    private static final Logger log = LoggerFactory.getLogger(TutorTopicService.class);

    private final TopicService topicService;
    private final GptService gptService;
    private final TutorConversationService conversationService;
    private final TutorMessageService messageService;
    private final UserServiceImpl userService;

    private long totalPromptTokens = 0;
    private long totalResponseTokens = 0;
    private long totalRequests = 0;

    public TutorTopicService(
            TopicService topicService,
            GptService gptService,
            TutorConversationService conversationService,
            TutorMessageService messageService,
            UserServiceImpl userService) {

        this.topicService = topicService;
        this.gptService = gptService;
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.userService = userService;
    }

    private String getFirstName(User user) {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return "aluno";
        }
        String fullName = user.getFullName().trim();
        int spaceIndex = fullName.indexOf(' ');
        return spaceIndex > 0 ? fullName.substring(0, spaceIndex) : fullName;
    }

    private String sanitizeText(String text) {
        if (text == null) return "";
        String sanitized = Normalizer.normalize(text, Normalizer.Form.NFC);
        sanitized = sanitized.replaceAll("[^\\x00-\\x7F\\p{L}\\p{N}\\p{P}\\p{Z}]", "");
        sanitized = sanitized.replaceAll("[áâãàäÁÂÃÀÄ]", "a");
        sanitized = sanitized.replaceAll("[éêèëÉÊÈË]", "e");
        sanitized = sanitized.replaceAll("[íîìïÍÎÌÏ]", "i");
        sanitized = sanitized.replaceAll("[óôõòöÓÔÕÒÖ]", "o");
        sanitized = sanitized.replaceAll("[úûùüÚÛÙÜ]", "u");
        sanitized = sanitized.replaceAll("[çÇ]", "c");
        sanitized = sanitized.replaceAll("[ñÑ]", "n");
        return sanitized;
    }

    private int estimateTokens(String text) {
        if (text == null) return 0;
        return (int) Math.ceil(text.length() / 3.0);
    }

    @Transactional
    public String ask(TutorRequest request) throws Exception {

        if (request == null) {
            throw new RuntimeException("Request invalido");
        }

        User user = userService.findById(request.getUserId());
        if (user == null) {
            throw new UserNotFoundException("Usuario nao encontrado");
        }

        Topic topic = topicService.findById(request.getTopicId());
        if (topic == null) {
            throw new TopicNotFoundException("Topico nao encontrado");
        }

        TutorConversation conversation = conversationService.getOrCreateForTopic(request.getUserId(), topic);

        String topicName = topic.getName();
        String firstName = getFirstName(user);

        log.info("[TOPIC] Usuario {} - Topico: {}", request.getUserId(), topicName);
        log.info("[TOPIC] Mensagem: {}", request.getMessage());

        if (request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
            messageService.saveUserMessage(conversation, request.getMessage());
        }

        List<TutorMessage> history = conversationService.getLastTopicMessages(
                request.getUserId(),
                request.getTopicId(),
                10
        );

        log.info("[TOPIC] Historico carregado: {} mensagens", history.size());

        String prompt = buildPrompt(
                topic,
                request.getMessage(),
                history,
                firstName,
                topicName
        );

        String sanitizedPrompt = sanitizeText(prompt);

        int estimatedPromptTokens = estimateTokens(sanitizedPrompt);
        totalPromptTokens += estimatedPromptTokens;

        long startTime = System.currentTimeMillis();
        String aiResponse = gptService.askAssistant(sanitizedPrompt);
        long endTime = System.currentTimeMillis();

        String sanitizedResponse = sanitizeText(aiResponse);

        int estimatedResponseTokens = estimateTokens(sanitizedResponse);
        totalRequests++;
        totalResponseTokens += estimatedResponseTokens;

        log.info("[TOPIC] Tokens: {} envio / {} resposta | {}ms | Total: {} reqs",
                estimatedPromptTokens, estimatedResponseTokens, (endTime - startTime), totalRequests);

        messageService.saveAssistantMessage(conversation, sanitizedResponse);
        conversation.setUpdatedAt(LocalDateTime.now());

        return sanitizedResponse;
    }

    private String buildPrompt(
            Topic topic,
            String userMessage,
            List<TutorMessage> history,
            String firstName,
            String topicName) {

        StringBuilder prompt = new StringBuilder();

        // =========================================================
        // SISTEMA E IDENTIDADE
        // =========================================================
        prompt.append("Voce e um tutor super inteligente e atencioso, especializado em ").append(topicName).append(".\n");
        prompt.append("Seu nome e Tutor AI. Voce esta ajudando ").append(firstName).append(".\n");
        prompt.append("Seja paciente, didatico e encorajador.\n");
        prompt.append("Use \\( \\) para formulas e \\[ \\] para equacoes. NUNCA use $ ou $$.\n\n");

        // =========================================================
        // INSTRUÇÕES CRÍTICAS SOBRE O HISTÓRICO
        // =========================================================
        prompt.append("=== ATENCAO: USE O HISTORICO ABAIXO ===\n");
        prompt.append("O historico mostra a conversa completa ate agora.\n");
        prompt.append("VOCE DEVE analisar cada mensagem para entender o contexto.\n");
        prompt.append("O aluno esta RESPONDENDO a ultima pergunta que voce fez.\n");
        prompt.append("Relacione a resposta atual com a pergunta anterior.\n");
        prompt.append("NAO ignore o historico. NAO comece a conversa do zero.\n");
        prompt.append("Seja inteligente: conecte a resposta do aluno com a ultima pergunta.\n\n");

        // =========================================================
        // HISTORICO COMPLETO
        // =========================================================
        if (!history.isEmpty()) {
            prompt.append("=== HISTORICO DA CONVERSA ===\n");
            for (int i = 0; i < history.size(); i++) {
                TutorMessage msg = history.get(i);
                String role = msg.getRole() == MessageRole.USER ? firstName : "TUTOR";
                String content = msg.getContent();
                if (content != null) {
                    prompt.append(i+1).append(". ").append(role).append(": ").append(content).append("\n");
                }
            }
            prompt.append("=== FIM DO HISTORICO ===\n\n");

            // Destacar a ultima mensagem do tutor
            for (int i = history.size() - 1; i >= 0; i--) {
                TutorMessage msg = history.get(i);
                if (msg.getRole() == MessageRole.ASSISTANT) {
                    String lastTutorMsg = msg.getContent();
                    if (lastTutorMsg != null && lastTutorMsg.length() > 200) {
                        lastTutorMsg = lastTutorMsg.substring(0, 200) + "...";
                    }
                    prompt.append("**ULTIMA PERGUNTA QUE VOCE FEZ:** \"").append(lastTutorMsg).append("\"\n");
                    prompt.append("O aluno esta RESPONDENDO a esta pergunta agora.\n\n");
                    break;
                }
            }
        } else {
            prompt.append("(Inicio da conversa. Nao ha historico anterior.)\n\n");
        }

        // =========================================================
        // REGRAS DE COMPORTAMENTO
        // =========================================================
        prompt.append("=== REGRAS OBRIGATORIAS ===\n");
        prompt.append("1. SEMPRE analise o historico antes de responder.\n");
        prompt.append("2. Compare a mensagem do aluno com a ultima pergunta que voce fez.\n");
        prompt.append("3. Se o aluno acertou -> Parabenize e va para o proximo conceito.\n");
        prompt.append("4. Se o aluno errou -> Corrija gentilmente e de uma dica.\n");
        prompt.append("5. Se o aluno disse \"nao sei\" ou \"nao entendi\" -> Ensine de outra forma, de um exemplo.\n");
        prompt.append("6. Se o aluno respondeu algo aleatorio -> Reconecte com a ultima pergunta.\n");
        prompt.append("7. SEMPRE termine com uma nova pergunta ou desafio.\n");
        prompt.append("8. Use o nome ").append(firstName).append(" nas respostas.\n\n");

        // =========================================================
        // CONTEUDO DO TOPICO
        // =========================================================
        if (topic.getDescription() != null && !topic.getDescription().isEmpty()) {
            String desc = topic.getDescription();
            if (desc.length() > 400) {
                desc = desc.substring(0, 400) + "...";
            }
            prompt.append("=== CONTEUDO DO TOPICO ===\n");
            prompt.append(desc).append("\n\n");
        }

        // =========================================================
        // EXEMPLO DE ANALISE (para guiar o GPT)
        // =========================================================
        prompt.append("=== EXEMPLO DE COMO ANALISAR ===\n");
        prompt.append("Se o historico mostrar:\n");
        prompt.append("TUTOR: Qual e a derivada de x²?\n");
        prompt.append("ALUNO: 2x\n");
        prompt.append("TUTOR: Excelente! Agora qual e a derivada de x³?\n");
        prompt.append("ALUNO: 3x²\n");
        prompt.append("Analise: O aluno acertou a ultima pergunta.\n");
        prompt.append("Resposta: Parabenize e faca a proxima pergunta.\n\n");
        prompt.append("Se o historico mostrar:\n");
        prompt.append("TUTOR: Qual e a derivada de x²?\n");
        prompt.append("ALUNO: Nao sei\n");
        prompt.append("Analise: O aluno esta com dificuldade.\n");
        prompt.append("Resposta: Explique a regra, de um exemplo, e faca uma pergunta mais simples.\n\n");

        // =========================================================
        // PERGUNTA ATUAL
        // =========================================================
        prompt.append("=== RESPOSTA ATUAL DO ALUNO ===\n");
        prompt.append(firstName).append(" disse: \"").append(userMessage).append("\"\n\n");
        prompt.append("=== SUA RESPOSTA (TUTOR) ===\n");
        prompt.append("TUTOR:");

        return prompt.toString();
    }
}