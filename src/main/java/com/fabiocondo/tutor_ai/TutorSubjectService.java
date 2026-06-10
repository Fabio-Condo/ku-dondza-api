package com.fabiocondo.tutor_ai;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.GptService;
import com.fabiocondo.service.impl.SubjectServiceImpl;
import com.fabiocondo.service.impl.TopicService;
import com.fabiocondo.service.impl.UserServiceImpl;
import com.fabiocondo.tutor_ai.conversation.TutorConversation;
import com.fabiocondo.tutor_ai.conversation.TutorConversationService;
import com.fabiocondo.tutor_ai.message.TutorMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TutorSubjectService {

    private static final Logger log = LoggerFactory.getLogger(TutorSubjectService.class);

    private final SubjectServiceImpl subjectService;
    private final TopicService topicService;
    private final GptService gptService;
    private final TutorConversationService conversationService;
    private final TutorMessageService messageService;
    private final UserServiceImpl userService;

    // Padrões para identificar tópicos na pergunta
    private static final Pattern TOPIC_PATTERNS = Pattern.compile(
            "(?i)(derivada|integral|limite|logaritmo|exponencial|trigonometria|geometria|matriz|determinante|função|equação|polinômio|fração|porcentagem|estatística|probabilidade)"
    );

    public TutorSubjectService(
            SubjectServiceImpl subjectService,
            TopicService topicService,
            GptService gptService,
            TutorConversationService conversationService,
            TutorMessageService messageService,
            UserServiceImpl userService) {

        this.subjectService = subjectService;
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

    @Transactional
    public String ask(TutorRequest request) throws Exception {

        if (request == null) {
            throw new RuntimeException("Request inválido");
        }

        User user = userService.findById(request.getUserId());
        if (user == null) {
            throw new UserNotFoundException("Usuário não encontrado");
        }

        Subject subject = subjectService.findById(request.getSubjectId());
        if (subject == null) {
            throw new SubjectNotFoundException("Disciplina não encontrada");
        }

        String subjectName = subject.getName();
        String firstName = getFirstName(user);

        log.info("[SUBJECT] Usuário {} - Disciplina: {}", request.getUserId(), subjectName);

        // Salva a mensagem do usuário na conversa da disciplina
        TutorConversation subjectConversation =
                conversationService.getOrCreateForSubject(request.getUserId(), subject);

        if (request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
            messageService.saveUserMessage(subjectConversation, request.getMessage());
        }

        // Busca os tópicos da disciplina
        List<Topic> subjectTopics = topicService.getBySubjectId(subject.getId());

        // Tenta identificar qual tópico o aluno está perguntando
        Topic identifiedTopic = identifyTopic(request.getMessage(), subjectTopics);

        String prompt = buildPrompt(
                subject,
                subjectTopics,
                identifiedTopic,
                request.getMessage(),
                user,
                subjectName
        );

        String aiResponse = gptService.askAssistant(prompt);

        // Se identificou um tópico específico, redireciona para o TutorTopicService
        if (identifiedTopic != null) {
            String redirectionMessage = buildRedirectionMessage(
                    firstName,
                    subjectName,
                    identifiedTopic.getName(),
                    aiResponse
            );

            // Salva a resposta do tutor da disciplina
            messageService.saveAssistantMessage(subjectConversation, redirectionMessage);
            subjectConversation.setUpdatedAt(LocalDateTime.now());

            log.info("[SUBJECT] Redirecionando usuário {} para o tópico: {}",
                    request.getUserId(), identifiedTopic.getName());

            return redirectionMessage;
        } else {
            // Resposta geral da disciplina (sem redirecionamento específico)
            messageService.saveAssistantMessage(subjectConversation, aiResponse);
            subjectConversation.setUpdatedAt(LocalDateTime.now());
            return aiResponse;
        }
    }

    private Topic identifyTopic(String message, List<Topic> topics) {
        if (message == null || topics == null || topics.isEmpty()) {
            return null;
        }

        String lowerMessage = message.toLowerCase();

        // Primeiro: tenta encontrar correspondência exata por nome do tópico
        for (Topic topic : topics) {
            String topicName = topic.getName().toLowerCase();
            if (lowerMessage.contains(topicName)) {
                return topic;
            }
        }

        // Segundo: usa padrões de palavras-chave
        Matcher matcher = TOPIC_PATTERNS.matcher(lowerMessage);
        if (matcher.find()) {
            String keyword = matcher.group();
            for (Topic topic : topics) {
                if (topic.getName().toLowerCase().contains(keyword)) {
                    return topic;
                }
            }
        }

        return null;
    }

    private String buildRedirectionMessage(String firstName, String subjectName,
                                           String topicName, String aiResponse) {
        return String.format(
                "%s\n\n---\n\n🎯 **Vamos aprofundar, %s!** Percebi que sua pergunta é sobre **%s**, que faz parte de **%s**.\n\n" +
                        "✅ **Sua resposta acima** já te dá uma boa base sobre o assunto.\n\n" +
                        "📚 **Para se tornar um especialista no tema**, vou te redirecionar para o **Tutor Especialista em %s**.\n\n" +
                        "Lá você terá:\n" +
                        "• Acompanhamento contínuo das suas dúvidas\n" +
                        "• Histórico completo do seu progresso\n" +
                        "• Explicações mais aprofundadas\n" +
                        "• Exercícios resolvidos passo a passo\n" +
                        "• Respostas com formatação profissional (LaTeX, tabelas, gráficos)\n\n" +
                        "**Iniciando atendimento no tópico de %s...** 🚀\n\n" +
                        "---\n" +
                        "💡 *Dica: Quanto mais específica sua pergunta, melhor posso te ajudar!*\n\n" +
                        "*(A resposta acima é um preview do que o especialista pode te oferecer)*",
                aiResponse, firstName, topicName, subjectName,
                topicName, topicName
        );
    }

    private String buildPrompt(
            Subject subject,
            List<Topic> topics,
            Topic identifiedTopic,
            String userMessage,
            User user,
            String subjectName) {

        StringBuilder prompt = new StringBuilder();
        String firstName = getFirstName(user);

        // =========================================================
        // SISTEMA E REGRAS
        // =========================================================
        prompt.append("Você é o Tutor da Disciplina **").append(subjectName).append("** na plataforma Dikahub.\n");
        prompt.append("Está conversando com ").append(firstName).append(".\n\n");

        prompt.append("SEU PAPEL:\n");
        prompt.append("- Seja acolhedor e ajude o aluno a navegar pela disciplina\n");
        prompt.append("- Responda perguntas gerais sobre a disciplina\n");
        prompt.append("- IDENTIFIQUE qual tópico específico a pergunta se refere\n");
        prompt.append("- Se identificar um tópico, responda brevemente (1-2 parágrafos) e depois REDIRECIONE para o tutor especialista\n");
        prompt.append("- Se NÃO identificar um tópico específico, responda normalmente como um tutor geral\n\n");

        prompt.append("VALORES FUNDAMENTAIS:\n");
        prompt.append("1. **ÚTIL** → Responda tudo que o aluno perguntar dentro da disciplina\n");
        prompt.append("2. **CLARO** → Explique de forma simples, com exemplos práticos\n");
        prompt.append("3. **COMPLETO** → Dê explicações que ajudem o aluno a entender\n");
        prompt.append("4. **ENTUSIASMO** → Mostre empolgação por ensinar\n\n");

        // =========================================================
        // FORMATAÇÃO LaTeX COMPLETA
        // =========================================================
        prompt.append("REGRAS DE FORMATAÇÃO LaTeX (OBRIGATÓRIAS):\n\n");

        prompt.append("📐 EXPRESSÕES MATEMÁTICAS INLINE:\n");
        prompt.append("Use \\( \\) para fórmulas no texto. Exemplos:\n");
        prompt.append("- \"A fórmula da área do círculo é \\(A = \\pi r^2\\)\"\n");
        prompt.append("- \"Resolvendo \\(x^2 - 5x + 6 = 0\\), temos \\(x = 2\\) ou \\(x = 3\\)\"\n\n");

        prompt.append("📊 EXPRESSÕES DESTACADAS (EQUAÇÕES):\n");
        prompt.append("Use \\[ \\] para equações em linha própria:\n");
        prompt.append("\\[\n");
        prompt.append("\\int_{0}^{\\infty} e^{-x^2} dx = \\frac{\\sqrt{\\pi}}{2}\n");
        prompt.append("\\]\n\n");

        prompt.append("📋 TABELAS (use quando comparar dados):\n");
        prompt.append("\\[\n");
        prompt.append("\\begin{array}{|c|c|}\n");
        prompt.append("\\hline\n");
        prompt.append("\\textbf{x} & \\textbf{f(x) = x^2} \\\\\n");
        prompt.append("\\hline\n");
        prompt.append("1 & 1 \\\\\n");
        prompt.append("2 & 4 \\\\\n");
        prompt.append("3 & 9 \\\\\n");
        prompt.append("\\hline\n");
        prompt.append("\\end{array}\n");
        prompt.append("\\]\n\n");

        prompt.append("🔢 DIVISÃO SINTÉTICA (RUFFINI):\n");
        prompt.append("\\[\n");
        prompt.append("\\begin{array}{r|rrrr}\n");
        prompt.append("2 & 1 & 3 & -4 & -12 \\\\\n");
        prompt.append("  &   & 2 & 10 & 12 \\\\\n");
        prompt.append("\\hline\n");
        prompt.append("  & 1 & 5 & 6 & 0\n");
        prompt.append("\\end{array}\n");
        prompt.append("\\]\n\n");

        prompt.append("📐 MATRIZES E DETERMINANTES:\n");
        prompt.append("- Matriz: \\[ \\begin{pmatrix} a & b \\\\ c & d \\end{pmatrix} \\]\n");
        prompt.append("- Determinante: \\[ \\begin{vmatrix} a & b \\\\ c & d \\end{vmatrix} = ad - bc \\]\n\n");

        prompt.append("🔧 SISTEMAS DE EQUAÇÕES:\n");
        prompt.append("\\[\n");
        prompt.append("\\begin{cases}\n");
        prompt.append("2x + 3y = 7 \\\\\n");
        prompt.append("x - y = 1\n");
        prompt.append("\\end{cases}\n");
        prompt.append("\\]\n\n");

        prompt.append("🧮 FRAÇÕES E OPERADORES:\n");
        prompt.append("- Fração: \\(\\frac{a}{b}\\)\n");
        prompt.append("- Raiz: \\(\\sqrt{x}\\) ou \\(\\sqrt[n]{x}\\)\n");
        prompt.append("- Somatório: \\(\\sum_{i=1}^{n} i\\)\n");
        prompt.append("- Integral: \\(\\int_{a}^{b} f(x) dx\\)\n");
        prompt.append("- Derivada: \\(\\frac{df}{dx}\\)\n");
        prompt.append("- Limite: \\(\\lim_{x \\to 0} \\frac{\\sin x}{x} = 1\\)\n\n");

        prompt.append("📝 LISTAS E ORGANIZAÇÃO:\n");
        prompt.append("- Use listas numeradas para passos sequenciais:\n");
        prompt.append("  **1.** Primeiro passo\n");
        prompt.append("  **2.** Segundo passo\n");
        prompt.append("- Use marcadores para itens relacionados:\n");
        prompt.append("  * Item 1\n");
        prompt.append("  * Item 2\n\n");

        prompt.append("🎨 FORMATAÇÃO GERAL:\n");
        prompt.append("- Use **negrito** para conceitos importantes\n");
        prompt.append("- Use *itálico* para ênfase\n");
        prompt.append("- Use `código` para fórmulas ou comandos\n");
        prompt.append("- Use emojis com moderação (😊, 📚, 🎯, 💡, 🚀)\n\n");

        prompt.append("⚠️ IMPORTANTE:\n");
        prompt.append("- NUNCA use $ ou $$ - use apenas \\( \\) e \\[ \\]\n");
        prompt.append("- SEMPRE verifique a formatação LaTeX antes de responder\n");
        prompt.append("- Seja breve e objetivo (máximo 3-4 parágrafos por resposta)\n\n");

        // =========================================================
        // CONTEÚDO DA DISCIPLINA
        // =========================================================
        prompt.append("📚 DISCIPLINA: **").append(subjectName).append("**\n\n");

        if (subject.getDescription() != null && !subject.getDescription().isEmpty()) {
            prompt.append("📖 DESCRIÇÃO DA DISCIPLINA:\n");
            prompt.append(subject.getDescription()).append("\n\n");
        }

        prompt.append("TÓPICOS DA DISCIPLINA ").append(subjectName.toUpperCase()).append(":\n");
        for (Topic topic : topics) {
            prompt.append("- **").append(topic.getName()).append("**\n");
            if (topic.getDescription() != null && !topic.getDescription().isEmpty()) {
                String desc = topic.getDescription();
                if (desc.length() > 100) {
                    desc = desc.substring(0, 100) + "...";
                }
                prompt.append("  ").append(desc).append("\n");
            }
        }
        prompt.append("\n");

        // =========================================================
        // ESTRATÉGIA BASEADA NO TÓPICO IDENTIFICADO
        // =========================================================
        if (identifiedTopic != null) {
            prompt.append("🔍 TÓPICO IDENTIFICADO: **").append(identifiedTopic.getName()).append("**\n");
            prompt.append("ESTRATÉGIA:\n");
            prompt.append("- Responda a pergunta de forma clara e útil (1-2 parágrafos)\n");
            prompt.append("- Mostre entusiasmo pelo tópico identificado\n");
            prompt.append("- Depois da resposta, anuncie o redirecionamento para o especialista\n");
            prompt.append("- O redirecionamento será feito automaticamente pelo sistema\n\n");
        } else {
            prompt.append("❓ NENHUM TÓPICO ESPECÍFICO IDENTIFICADO.\n");
            prompt.append("ESTRATÉGIA:\n");
            prompt.append("- Responda normalmente como um tutor geral da disciplina\n");
            prompt.append("- Ajude o aluno com dúvidas gerais sobre a disciplina\n");
            prompt.append("- Dê exemplos de tópicos que ele pode estudar\n");
            prompt.append("- Não há necessidade de redirecionamento\n\n");
        }

        // =========================================================
        // PERGUNTA E INSTRUÇÕES FINAIS
        // =========================================================
        prompt.append("PERGUNTA DO ALUNO: ").append(userMessage).append("\n\n");

        prompt.append("INSTRUÇÕES FINAIS (OBRIGATÓRIAS):\n");
        prompt.append("1. Use formatação LaTeX correta (\\( \\) para inline, \\[ \\] para destaques)\n");
        prompt.append("2. Seja acolhedor e use o nome ").append(firstName).append("\n");
        prompt.append("3. Responda de forma clara, com exemplos quando possível\n");
        prompt.append("4. Se identificou um tópico, responda brevemente (1-2 parágrafos)\n");
        prompt.append("5. NUNCA use $ ou $$ - use apenas \\( \\) e \\[ \\]\n");
        prompt.append("6. Mantenha o foco na disciplina **").append(subjectName).append("**\n");
        prompt.append("7. Mostre entusiasmo por ajudar o aluno a aprender!\n");

        return prompt.toString();
    }
}