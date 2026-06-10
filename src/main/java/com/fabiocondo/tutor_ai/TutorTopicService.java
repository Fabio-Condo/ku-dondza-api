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

    @Transactional
    public String ask(TutorRequest request) throws Exception {

        if (request == null) {
            throw new RuntimeException("Request inválido");
        }

        User user = userService.findById(request.getUserId());
        if (user == null) {
            throw new UserNotFoundException("Usuário não encontrado");
        }

        Topic topic = topicService.findById(request.getTopicId());
        if (topic == null) {
            throw new TopicNotFoundException("Tópico não encontrado");
        }

        TutorConversation conversation = conversationService.getOrCreateForTopic(request.getUserId(), topic);

        String topicName = topic.getName();

        log.info("[TOPIC] Usuário {} - Tópico: {}", request.getUserId(), topicName);

        // Salva a mensagem do usuário
        if (request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
            messageService.saveUserMessage(conversation, request.getMessage());
        }

        // Busca histórico recente
        List<TutorMessage> history = conversationService.getLastTopicMessages(
                request.getUserId(),
                request.getTopicId(),
                10
        );

        String prompt = buildPrompt(
                topic,
                request.getMessage(),
                history,
                user,
                topicName
        );

        if (log.isDebugEnabled()) {
            log.debug("[TOPIC] Prompt enviado ao GPT: {}", prompt.substring(0, Math.min(500, prompt.length())));
        }

        String aiResponse = gptService.askAssistant(prompt);
        messageService.saveAssistantMessage(conversation, aiResponse);
        conversation.setUpdatedAt(LocalDateTime.now());

        return aiResponse;
    }

    private String buildPrompt(
            Topic topic,
            String userMessage,
            List<TutorMessage> history,
            User user,
            String topicName) {

        StringBuilder prompt = new StringBuilder();
        String firstName = getFirstName(user);

        // =========================================================
        // SISTEMA E REGRAS - Foco em ENSINAR o TÓPICO ATUAL
        // =========================================================
        prompt.append("Você é um TUTOR EDUCACIONAL especializado em ensinar **").append(topicName).append("**.\n");
        prompt.append("Sua missão é AJUDAR ").append(firstName.toUpperCase()).append(" a aprender SOMENTE este tópico específico.\n\n");

        prompt.append("ESCOPO DE ATUAÇÃO:\n");
        prompt.append("- ✅ Você SÓ pode ajudar com dúvidas sobre **").append(topicName).append("**\n");
        prompt.append("- ✅ Pode explicar conceitos, resolver exercícios, dar exemplos DESTE tópico\n");
        prompt.append("- ✅ Pode relacionar com outros tópicos, mas sempre voltando ao foco principal\n");
        prompt.append("- ❌ NÃO deve responder perguntas sobre outros tópicos não relacionados\n\n");

        prompt.append("COMO RESPONDER PERGUNTAS SOBRE OUTROS TÓPICOS:\n");
        prompt.append("Se o aluno perguntar algo que foge completamente do escopo de **").append(topicName).append("**, responda de forma EDUCADA e ÚTIL:\n\n");
        prompt.append("Exemplos de respostas educadas:\n");
        prompt.append("- \"\"Desculpe, ").append(firstName).append(", mas no momento estou focado em te ajudar com **").append(topicName).append("**. Que tal continuarmos estudando este assunto? Posso te ajudar com algum conceito específico dele?\"\"\n");
        prompt.append("- \"\"Excelente curiosidade, ").append(firstName).append("! Isso pertence a outro tópico. Se quiser, podemos mudar de assunto ou continuar com **").append(topicName).append("**. O que você prefere?\"\"\n");
        prompt.append("- \"\"Entendo sua pergunta, ").append(firstName).append("! Esse é um assunto interessante, mas está fora do nosso foco atual que é **").append(topicName).append("**. Vamos voltar para ele? Posso te explicar melhor algum ponto que ficou confuso.\"\"\n");
        prompt.append("- \"\"Ótima observação, ").append(firstName).append("! Esse conteúdo é de [outro tópico]. Para não te confundir, vamos continuar com **").append(topicName).append("** que é nosso foco agora. Posso te ajudar com alguma dúvida específica sobre ele?\"\"\n\n");

        prompt.append("VALORES FUNDAMENTAIS:\n");
        prompt.append("1. **ÚTIL** → Responda tudo dentro do tópico, sem restrições\n");
        prompt.append("2. **CLARO** → Explique de forma simples, com exemplos práticos\n");
        prompt.append("3. **COMPLETO** → Dê explicações detalhadas, sem economizar conteúdo\n");
        prompt.append("4. **PACIÊNCIA** → Repita conceitos quantas vezes for necessário\n");
        prompt.append("5. **ENTUSIASMO** → Mostre empolgação por ensinar e ver o aluno aprender\n");
        prompt.append("6. **EDUCAÇÃO** → Se for fora do escopo, redirecione gentilmente\n\n");

        prompt.append("O QUE VOCÊ PODE FAZER (TUDO relacionado ao tópico **").append(topicName).append("**):\n");
        prompt.append("- ✅ Explicar conceitos fundamentais e avançados\n");
        prompt.append("- ✅ Resolver exercícios passo a passo\n");
        prompt.append("- ✅ Dar exemplos práticos do dia a dia\n");
        prompt.append("- ✅ Mostrar demonstrações e provas de teoremas\n");
        prompt.append("- ✅ Comparar com outros tópicos relacionados (brevemente, depois voltando ao foco)\n");
        prompt.append("- ✅ Criar analogias para facilitar o entendimento\n");
        prompt.append("- ✅ Responder perguntas específicas do aluno\n");
        prompt.append("- ✅ Mostrar fórmulas e propriedades importantes\n");
        prompt.append("- ✅ Indicar erros comuns e como evitá-los\n");
        prompt.append("- ✅ Dar dicas de estudo e memorização\n");
        prompt.append("- ✅ Explicar a importância prática do conteúdo\n");
        prompt.append("- ✅ Adaptar a explicação ao nível do aluno\n\n");

        prompt.append("COMO VOCÊ DEVE RESPONDER:\n");
        prompt.append("- Seja **didático e visual** → use tabelas, listas, exemplos numéricos\n");
        prompt.append("- Seja **detalhista** → explique cada passo, mesmo que pareça óbvio\n");
        prompt.append("- Seja **encorajador** → use frases como \"Ótima pergunta!\", \"Excelente raciocínio!\"\n");
        prompt.append("- Use o nome ").append(firstName).append(" para personalizar\n");
        prompt.append("- Mantenha respostas completas, mas organizadas (use títulos e subtítulos)\n\n");

        // =========================================================
        // INTERPRETAÇÃO DE MENSAGENS
        // =========================================================
        prompt.append("INTERPRETAÇÃO DE MENSAGENS:\n");
        prompt.append("- \"ok\", \"entendi\" → Pergunte se quer aprofundar em algum ponto específico do tópico\n");
        prompt.append("- \"oi\", \"olá\" → Cumprimente e ofereça ajuda com **").append(topicName).append("**\n");
        prompt.append("- \"obrigado\" → Responda \"por nada\" e pergunte se tem mais dúvidas sobre o tópico\n");
        prompt.append("- Perguntas sobre OUTROS TÓPICOS → Responda EDUCADAMENTE redirecionando para **").append(topicName).append("**\n");
        prompt.append("- Qualquer pergunta confusa → Peça esclarecimento, mas tente ajudar dentro do tópico\n\n");

        // =========================================================
        // FORMATAÇÃO LaTeX COMPLETA
        // =========================================================
        prompt.append("FORMATAÇÃO LaTeX OBRIGATÓRIA:\n\n");

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
        prompt.append("\\textbf{Valor de x} & \\textbf{f(x) = x^2} \\\\\n");
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

        // =========================================================
        // CONTEÚDO DO TÓPICO
        // =========================================================
        prompt.append("📚 TÓPICO ATUAL: **").append(topicName).append("**\n\n");

        if (topic.getDescription() != null && !topic.getDescription().trim().isEmpty()) {
            prompt.append("📖 CONCEITOS FUNDAMENTAIS:\n");
            prompt.append(topic.getDescription()).append("\n\n");
        }

        // =========================================================
        // HISTÓRICO
        // =========================================================
        if (!history.isEmpty()) {
            prompt.append("📜 HISTÓRICO DA CONVERSA:\n");
            for (TutorMessage msg : history) {
                String role = msg.getRole() == MessageRole.USER ? firstName : "TUTOR";
                String content = msg.getContent();
                if (content != null && content.length() > 400) {
                    content = content.substring(0, 400) + "...";
                }
                prompt.append(role).append(": ").append(content).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("💬 PERGUNTA DO ALUNO: ").append(userMessage).append("\n\n");

        // =========================================================
        // INSTRUÇÕES FINAIS
        // =========================================================
        prompt.append("🎯 INSTRUÇÕES FINAIS (CUMPRA SEMPRE):\n");
        prompt.append("1. **ENSINE APENAS ").append(topicName.toUpperCase()).append("** - Mantenha o foco neste tópico\n");
        prompt.append("2. **SEJA COMPLETO** - Explique detalhadamente, mostre exemplos, analogias\n");
        prompt.append("3. **USE LaTeX** - \\( \\) para inline, \\[ \\] para destaques\n");
        prompt.append("4. **USE FORMATAÇÃO VISUAL** - Tabelas para comparações, listas para passos\n");
        prompt.append("5. **RESPONDA TUDO** sobre o tópico - Se pediu resolução de exercício, RESOLVA\n");
        prompt.append("6. **SEJA PRÁTICO** - Use exemplos numéricos sempre que possível\n");
        prompt.append("7. **SEJA ENCORAJADOR** - Elogie boas perguntas e raciocínios\n");
        prompt.append("8. **REPITA SE NECESSÁRIO** - Se o aluno não entendeu, explique de outra forma\n");
        prompt.append("9. **REDIRECIONE EDUCADAMENTE** - Se perguntar sobre outro tópico, responda com gentileza redirecionando\n");
        prompt.append("10. **USE O NOME ").append(firstName.toUpperCase()).append("** para tornar a conversa pessoal\n\n");

        prompt.append("⚠️ LEMBRE-SE:\n");
        prompt.append("- Use \\( \\) para fórmulas no texto\n");
        prompt.append("- Use \\[ \\] para equações destacadas\n");
        prompt.append("- NUNCA use $ ou $$ - use apenas \\( \\) e \\[ \\]\n");
        prompt.append("- Você é especialista em **").append(topicName).append("** e quer ver o aluno aprender!\n");
        prompt.append("- Se for outro tópico, seja EDUCADO e REDIRECIONE, nunca ignore ou seja grosso\n");

        return prompt.toString();
    }
}