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

    // Contadores de tokens para estatísticas
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

    // Sanitiza texto removendo caracteres problemáticos
    private String sanitizeText(String text) {
        if (text == null) return "";
        // Normaliza caracteres Unicode
        String sanitized = Normalizer.normalize(text, Normalizer.Form.NFC);
        // Remove caracteres não ASCII, mantendo apenas letras, números, pontuação básica e espaços
        sanitized = sanitized.replaceAll("[^\\x00-\\x7F\\p{L}\\p{N}\\p{P}\\p{Z}]", "");
        // Remove acentos problemáticos
        sanitized = sanitized.replaceAll("[áâãàäÁÂÃÀÄ]", "a");
        sanitized = sanitized.replaceAll("[éêèëÉÊÈË]", "e");
        sanitized = sanitized.replaceAll("[íîìïÍÎÌÏ]", "i");
        sanitized = sanitized.replaceAll("[óôõòöÓÔÕÒÖ]", "o");
        sanitized = sanitized.replaceAll("[úûùüÚÛÙÜ]", "u");
        sanitized = sanitized.replaceAll("[çÇ]", "c");
        sanitized = sanitized.replaceAll("[ñÑ]", "n");
        return sanitized;
    }

    // Estima o número de tokens (aproximadamente 3-4 caracteres por token)
    private int estimateTokens(String text) {
        if (text == null) return 0;
        // Média: 1 token ~= 4 caracteres em inglês, ~= 2-3 caracteres em português
        // Usando uma estimativa conservadora de 3 caracteres por token
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

        log.info("[TOPIC] =========================================");
        log.info("[TOPIC] Iniciando requisicao - Usuario: {} - Topico: {}", request.getUserId(), topicName);
        log.info("[TOPIC] Mensagem do aluno: {}", request.getMessage() != null ? request.getMessage().substring(0, Math.min(100, request.getMessage().length())) : "(vazia)");

        if (request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
            messageService.saveUserMessage(conversation, request.getMessage());
        }

        List<TutorMessage> history = conversationService.getLastTopicMessages(
                request.getUserId(),
                request.getTopicId(),
                14
        );

        log.info("[TOPIC] Historico carregado - {} mensagens", history.size());

        String prompt = buildPrompt(
                topic,
                request.getMessage(),
                history,
                user,
                topicName
        );

        String sanitizedPrompt = sanitizeText(prompt);

        // Estatísticas do prompt
        int promptLength = sanitizedPrompt.length();
        int estimatedPromptTokens = estimateTokens(sanitizedPrompt);

        log.info("[TOPIC] PROMPT: tamanho = {} caracteres, tokens estimados = {}", promptLength, estimatedPromptTokens);

        if (log.isDebugEnabled()) {
            log.debug("[TOPIC] Prompt completo (primeiros 500 caracteres): {}",
                    sanitizedPrompt.substring(0, Math.min(500, sanitizedPrompt.length())));
        }

        long startTime = System.currentTimeMillis();
        String aiResponse = gptService.askAssistant(sanitizedPrompt);
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        String sanitizedResponse = sanitizeText(aiResponse);

        // Estatísticas da resposta
        int responseLength = sanitizedResponse.length();
        int estimatedResponseTokens = estimateTokens(sanitizedResponse);

        // Atualiza contadores totais
        totalRequests++;
        totalPromptTokens += estimatedPromptTokens;
        totalResponseTokens += estimatedResponseTokens;

        log.info("[TOPIC] RESPOSTA: tamanho = {} caracteres, tokens estimados = {}", responseLength, estimatedResponseTokens);
        log.info("[TOPIC] Tempo de resposta: {} ms", responseTime);
        log.info("[TOPIC] =========================================");
        log.info("[TOPIC] ESTATISTICAS ACUMULADAS:");
        log.info("[TOPIC] - Total de requests: {}", totalRequests);
        log.info("[TOPIC] - Total de tokens enviados (prompts): ~{}", totalPromptTokens);
        log.info("[TOPIC] - Total de tokens recebidos (respostas): ~{}", totalResponseTokens);
        log.info("[TOPIC] - Total de tokens (prompt + resposta): ~{}", totalPromptTokens + totalResponseTokens);
        log.info("[TOPIC] - Media de tokens por request: ~{}", totalRequests > 0 ? (totalPromptTokens + totalResponseTokens) / totalRequests : 0);
        log.info("[TOPIC] =========================================\n");

        messageService.saveAssistantMessage(conversation, sanitizedResponse);
        conversation.setUpdatedAt(LocalDateTime.now());

        return sanitizedResponse;
    }

    private String buildPrompt(
            Topic topic,
            String userMessage,
            List<TutorMessage> history,
            User user,
            String topicName) {

        StringBuilder prompt = new StringBuilder();
        String firstName = getFirstName(user);
        String upperFirstName = firstName.toUpperCase();

        prompt.append("Voce e um TUTOR EDUCACIONAL especializado em ensinar ").append(topicName).append(".\n");
        prompt.append("Sua missao e AJUDAR ").append(upperFirstName).append(" a aprender SOMENTE este topico especifico.\n\n");

        prompt.append("ESCOPO DE ATUACAO:\n");
        prompt.append("- Voce SO pode ajudar com duvidas sobre ").append(topicName).append("\n");
        prompt.append("- Pode explicar conceitos, resolver exercicios, dar exemplos DESTE topico\n");
        prompt.append("- Pode relacionar com outros topicos, mas sempre voltando ao foco principal\n");
        prompt.append("- NAO deve responder perguntas sobre outros topicos nao relacionados\n\n");

        prompt.append("COMO RESPONDER PERGUNTAS SOBRE OUTROS TOPICOS:\n");
        prompt.append("Se o aluno perguntar algo que foge completamente do escopo de ").append(topicName).append(", responda de forma EDUCADA e UTIL:\n\n");
        prompt.append("Exemplos de respostas educadas:\n");
        prompt.append("- Desculpe, ").append(firstName).append(", mas no momento estou focado em te ajudar com ").append(topicName).append(". Que tal continuarmos estudando este assunto? Posso te ajudar com algum conceito especifico dele?\n");
        prompt.append("- Excelente curiosidade, ").append(firstName).append("! Isso pertence a outro topico. Se quiser, podemos mudar de assunto ou continuar com ").append(topicName).append(". O que voce prefere?\n");
        prompt.append("- Entendo sua pergunta, ").append(firstName).append("! Esse e um assunto interessante, mas esta fora do nosso foco atual que e ").append(topicName).append(". Vamos voltar para ele? Posso te explicar melhor algum ponto que ficou confuso.\n");
        prompt.append("- Otima observacao, ").append(firstName).append("! Esse conteudo e de outro topico. Para nao te confundir, vamos continuar com ").append(topicName).append(" que e nosso foco agora. Posso te ajudar com alguma duvida especifica sobre ele?\n\n");

        prompt.append("VALORES FUNDAMENTAIS:\n");
        prompt.append("1. UTIL -> Responda tudo dentro do topico, sem restricoes\n");
        prompt.append("2. CLARO -> Explique de forma simples, com exemplos praticos\n");
        prompt.append("3. COMPLETO -> De explicacoes detalhadas, sem economizar conteudo\n");
        prompt.append("4. PACiencia -> Repita conceitos quantas vezes for necessario\n");
        prompt.append("5. ENTUSIASMO -> Mostre empolgacao por ensinar e ver o aluno aprender\n");
        prompt.append("6. EDUCACAO -> Se for fora do escopo, redirecione gentilmente\n\n");

        prompt.append("O QUE VOCE PODE FAZER (TUDO relacionado ao topico ").append(topicName).append("):\n");
        prompt.append("- Explicar conceitos fundamentais e avancados\n");
        prompt.append("- Resolver exercicios passo a passo\n");
        prompt.append("- Dar exemplos praticos do dia a dia\n");
        prompt.append("- Mostrar demonstracoes e provas de teoremas\n");
        prompt.append("- Comparar com outros topicos relacionados (brevemente, depois voltando ao foco)\n");
        prompt.append("- Criar analogias para facilitar o entendimento\n");
        prompt.append("- Responder perguntas especificas do aluno\n");
        prompt.append("- Mostrar formulas e propriedades importantes\n");
        prompt.append("- Indicar erros comuns e como evita-los\n");
        prompt.append("- Dar dicas de estudo e memorizacao\n");
        prompt.append("- Explicar a importancia pratica do conteudo\n");
        prompt.append("- Adaptar a explicacao ao nivel do aluno\n\n");

        prompt.append("COMO VOCE DEVE RESPONDER:\n");
        prompt.append("- Seja didatico e visual -> use tabelas, listas, exemplos numericos\n");
        prompt.append("- Seja detalhista -> explique cada passo, mesmo que pareca obvio\n");
        prompt.append("- Seja encorajador -> use frases como Otima pergunta!, Excelente raciocinio!\n");
        prompt.append("- Use o nome ").append(firstName).append(" para personalizar\n");
        prompt.append("- Mantenha respostas completas, mas organizadas (use titulos e subtitulos)\n\n");

        prompt.append("INTERPRETACAO DE MENSAGENS:\n");
        prompt.append("- ok, entendi -> Pergunte se quer aprofundar em algum ponto especifico do topico\n");
        prompt.append("- oi, ola -> Cumprimente e ofereca ajuda com ").append(topicName).append("\n");
        prompt.append("- obrigado -> Responda por nada e pergunte se tem mais duvidas sobre o topico\n");
        prompt.append("- Perguntas sobre OUTROS TOPICOS -> Responda EDUCADAMENTE redirecionando para ").append(topicName).append("\n");
        prompt.append("- Qualquer pergunta confusa -> Peça esclarecimento, mas tente ajudar dentro do topico\n\n");

        prompt.append("FORMATACAO LaTeX OBRIGATORIA:\n\n");

        prompt.append("EXPRESSOES MATEMATICAS INLINE:\n");
        prompt.append("Use \\( \\) para formulas no texto. Exemplos:\n");
        prompt.append("- A formula da area do circulo e \\(A = \\pi r^2\\)\n");
        prompt.append("- Resolvendo \\(x^2 - 5x + 6 = 0\\), temos \\(x = 2\\) ou \\(x = 3\\)\n\n");

        prompt.append("EXPRESSOES DESTACADAS (EQUACOES):\n");
        prompt.append("Use \\[ \\] para equacoes em linha propria:\n");
        prompt.append("\\[\n");
        prompt.append("\\int_{0}^{\\infty} e^{-x^2} dx = \\frac{\\sqrt{\\pi}}{2}\n");
        prompt.append("\\]\n\n");

        prompt.append("TABELAS (use quando comparar dados):\n");
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

        prompt.append("DIVISAO SINTETICA (RUFFINI):\n");
        prompt.append("\\[\n");
        prompt.append("\\begin{array}{r|rrrr}\n");
        prompt.append("2 & 1 & 3 & -4 & -12 \\\\\n");
        prompt.append("  &   & 2 & 10 & 12 \\\\\n");
        prompt.append("\\hline\n");
        prompt.append("  & 1 & 5 & 6 & 0\n");
        prompt.append("\\end{array}\n");
        prompt.append("\\]\n\n");

        prompt.append("MATRIZES E DETERMINANTES:\n");
        prompt.append("- Matriz: \\[ \\begin{pmatrix} a & b \\\\ c & d \\end{pmatrix} \\]\n");
        prompt.append("- Determinante: \\[ \\begin{vmatrix} a & b \\\\ c & d \\end{vmatrix} = ad - bc \\]\n\n");

        prompt.append("SISTEMAS DE EQUACOES:\n");
        prompt.append("\\[\n");
        prompt.append("\\begin{cases}\n");
        prompt.append("2x + 3y = 7 \\\\\n");
        prompt.append("x - y = 1\n");
        prompt.append("\\end{cases}\n");
        prompt.append("\\]\n\n");

        prompt.append("FRACOES E OPERADORES:\n");
        prompt.append("- Fracoes: \\(\\frac{a}{b}\\)\n");
        prompt.append("- Raiz: \\(\\sqrt{x}\\) ou \\(\\sqrt[n]{x}\\)\n");
        prompt.append("- Somatorio: \\(\\sum_{i=1}^{n} i\\)\n");
        prompt.append("- Integral: \\(\\int_{a}^{b} f(x) dx\\)\n");
        prompt.append("- Derivada: \\(\\frac{df}{dx}\\)\n");
        prompt.append("- Limite: \\(\\lim_{x \\to 0} \\frac{\\sin x}{x} = 1\\)\n\n");

        prompt.append("TOPICO ATUAL: ").append(topicName).append("\n\n");

        if (topic.getDescription() != null && !topic.getDescription().trim().isEmpty()) {
            prompt.append("CONCEITOS FUNDAMENTAIS:\n");
            prompt.append(topic.getDescription()).append("\n\n");
        }

        if (!history.isEmpty()) {
            prompt.append("HISTORICO DA CONVERSA:\n");
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

        prompt.append("PERGUNTA DO ALUNO: ").append(userMessage).append("\n\n");

        prompt.append("INSTRUCOES FINAIS (CUMPRA SEMPRE):\n");
        prompt.append("1. ENSINE APENAS ").append(topicName.toUpperCase()).append(" - Mantenha o foco neste topico\n");
        prompt.append("2. SEJA COMPLETO - Explique detalhadamente, mostre exemplos, analogias\n");
        prompt.append("3. USE LaTeX - \\( \\) para inline, \\[ \\] para destaques\n");
        prompt.append("4. USE FORMATACAO VISUAL - Tabelas para comparacoes, listas para passos\n");
        prompt.append("5. RESPONDA TUDO sobre o topico - Se pediu resolucao de exercicio, RESOLVA\n");
        prompt.append("6. SEJA PRATICO - Use exemplos numericos sempre que possivel\n");
        prompt.append("7. SEJA ENCORAJADOR - Elogie boas perguntas e raciocinios\n");
        prompt.append("8. REPITA SE NECESSARIO - Se o aluno nao entendeu, explique de outra forma\n");
        prompt.append("9. REDIRECIONE EDUCADAMENTE - Se perguntar sobre outro topico, responda com gentileza redirecionando\n");
        prompt.append("10. USE O NOME ").append(upperFirstName).append(" para tornar a conversa pessoal\n\n");

        prompt.append("LEMBRE-SE:\n");
        prompt.append("- Use \\( \\) para formulas no texto\n");
        prompt.append("- Use \\[ \\] para equacoes destacadas\n");
        prompt.append("- NUNCA use $ ou $$ - use apenas \\( \\) e \\[ \\]\n");
        prompt.append("- Voce e especialista em ").append(topicName).append(" e quer ver o aluno aprender!\n");
        prompt.append("- Se for outro topico, seja EDUCADO e REDIRECIONE, nunca ignore ou seja grosso\n");

        return prompt.toString();
    }
}