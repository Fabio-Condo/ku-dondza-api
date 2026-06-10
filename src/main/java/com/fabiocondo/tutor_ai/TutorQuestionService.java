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
public class TutorQuestionService {

    private static final Logger log = LoggerFactory.getLogger(TutorQuestionService.class);

    private final QuestionService questionService;
    private final GptService gptService;
    private final TutorConversationService conversationService;
    private final TutorMessageService messageService;
    private final UserServiceImpl userService;

    private static final Pattern IMAGE_EXTENSION_PATTERN = Pattern.compile(
            "(?i)\\.(jpg|jpeg|png|gif|bmp|svg|webp)(\\?|$)"
    );

    // Contadores de tokens para estatísticas
    private long totalPromptTokens = 0;
    private long totalResponseTokens = 0;
    private long totalRequests = 0;

    public TutorQuestionService(
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

    private String getFirstName(User user) {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return "aluno";
        }
        String fullName = user.getFullName().trim();
        int spaceIndex = fullName.indexOf(' ');
        return spaceIndex > 0 ? fullName.substring(0, spaceIndex) : fullName;
    }

    private String getTopicName(Question question) {
        if (question.getTopic() != null && question.getTopic().getName() != null) {
            return question.getTopic().getName();
        }
        return "este topico";
    }

    private boolean hasImage(Question question) {
        if (question.getUrlFile() == null || question.getUrlFile().trim().isEmpty()) {
            return false;
        }
        String urlFile = question.getUrlFile().trim();
        return IMAGE_EXTENSION_PATTERN.matcher(urlFile).find();
    }

    private boolean hasGraph(Question question) {
        return question.getMathExpressions() != null && !question.getMathExpressions().isEmpty();
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

        Question question = questionService.findById(request.getQuestionId());
        if (question == null) {
            throw new QuestionNotFoundException("Questao nao encontrada");
        }

        TutorConversation conversation = conversationService.getOrCreateForQuestion(request.getUserId(), question);

        String topicName = getTopicName(question);
        boolean hasGraph = hasGraph(question);
        boolean hasImage = hasImage(question);

        log.info("[QUESTION] =========================================");
        log.info("[QUESTION] Iniciando requisicao - Usuario: {} - Questao: {}", request.getUserId(), request.getQuestionId());
        log.info("[QUESTION] Topico: {} - Grafico: {} - Imagem: {}", topicName, hasGraph, hasImage);
        log.info("[QUESTION] Mensagem do aluno: {}", request.getMessage() != null ? request.getMessage().substring(0, Math.min(100, request.getMessage().length())) : "(vazia)");

        if (request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
            messageService.saveUserMessage(conversation, request.getMessage());
        }

        List<TutorMessage> history = conversationService.getLastQuestionMessages(
                request.getUserId(),
                request.getQuestionId(),
                14
        );

        log.info("[QUESTION] Historico carregado - {} mensagens", history.size());

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

        String prompt = buildPrompt(
                question,
                selectedAnswer,
                correctAnswer,
                request.getMessage(),
                history,
                user,
                topicName,
                hasGraph,
                hasImage
        );

        String sanitizedPrompt = sanitizeText(prompt);

        // Estatísticas do prompt
        int promptLength = sanitizedPrompt.length();
        int estimatedPromptTokens = estimateTokens(sanitizedPrompt);

        log.info("[QUESTION] PROMPT: tamanho = {} caracteres, tokens estimados = {}", promptLength, estimatedPromptTokens);

        if (log.isDebugEnabled()) {
            log.debug("[QUESTION] Prompt completo (primeiros 500 caracteres): {}",
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

        log.info("[QUESTION] RESPOSTA: tamanho = {} caracteres, tokens estimados = {}", responseLength, estimatedResponseTokens);
        log.info("[QUESTION] Tempo de resposta: {} ms", responseTime);
        log.info("[QUESTION] =========================================");
        log.info("[QUESTION] ESTATISTICAS ACUMULADAS:");
        log.info("[QUESTION] - Total de requests: {}", totalRequests);
        log.info("[QUESTION] - Total de tokens enviados (prompts): ~{}", totalPromptTokens);
        log.info("[QUESTION] - Total de tokens recebidos (respostas): ~{}", totalResponseTokens);
        log.info("[QUESTION] - Total de tokens (prompt + resposta): ~{}", totalPromptTokens + totalResponseTokens);
        log.info("[QUESTION] - Media de tokens por request: ~{}", totalRequests > 0 ? (totalPromptTokens + totalResponseTokens) / totalRequests : 0);
        log.info("[QUESTION] =========================================\n");

        messageService.saveAssistantMessage(conversation, sanitizedResponse);
        conversation.setUpdatedAt(LocalDateTime.now());

        return sanitizedResponse;
    }

    private String buildPrompt(
            Question question,
            Answer selectedAnswer,
            Answer correctAnswer,
            String userMessage,
            List<TutorMessage> history,
            User user,
            String topicName,
            boolean hasGraph,
            boolean hasImage) {

        StringBuilder prompt = new StringBuilder();
        String firstName = getFirstName(user);

        // =========================================================
        // SISTEMA E REGRAS
        // =========================================================
        prompt.append("Voce e o Tutor AI da plataforma Dikahub, especializado em ajudar alunos a RESOLVER EXERCICIOS.\n");
        prompt.append("Esta ajudando ").append(firstName).append(" com um EXERCICIO sobre **").append(topicName).append("**.\n\n");

        prompt.append("SEU PAPEL:\n");
        prompt.append("- Seja pedagogico, acolhedor e paciente\n");
        prompt.append("- Use o nome ").append(firstName).append(" naturalmente na conversa\n");
        prompt.append("- NUNCA de a resposta pronta, estimule o raciocinio\n");
        prompt.append("- Seja breve e objetivo (maximo 3-4 paragrafos por resposta)\n");
        prompt.append("- Faca perguntas que levem o aluno a descobrir a resposta sozinho\n\n");

        prompt.append("INTERPRETACAO DE MENSAGENS DO ALUNO:\n");
        prompt.append("Voce DEVE interpretar a intencao do aluno baseado no CONTEXTO da conversa. Exemplos:\n");
        prompt.append("- Se o aluno disse apenas ok, entendi, esta bem, certo, blz - isso e uma CONFIRMACAO. Responda perguntando se tem mais duvidas.\n");
        prompt.append("- Se o aluno disse oi, ola, bom dia - e um CUMPRIMENTO. Responda educadamente e pergunte como pode ajudar.\n");
        prompt.append("- Se o aluno disse obrigado, valeu - e um AGRADECIMENTO. Responda com por nada e pergunte se precisa de mais ajuda.\n");
        prompt.append("- Se o aluno disse algo como como voce esta? - e uma PERGUNTA SOBRE VOCE. Responda que esta bem e ofereca ajuda.\n");
        prompt.append("- Se o aluno disse algo fora do contexto - diga que so pode ajudar com **").append(topicName).append("**.\n");
        prompt.append("- Se a mensagem for muito confusa ou sem sentido - peca para reformular educadamente.\n");
        prompt.append("- Para qualquer outra mensagem, ajude com a questao normalmente.\n\n");

        // =========================================================
        // FONTES DE INFORMACAO
        // =========================================================
        prompt.append("FONTES DE INFORMACAO DISPONIVEIS PARA VOCE:\n");
        if (question.getTip() != null && !question.getTip().trim().isEmpty()) {
            prompt.append("1. DICA OFICIAL: ").append(question.getTip()).append("\n");
        }
        if (question.getSolution() != null && !question.getSolution().trim().isEmpty()) {
            prompt.append("2. SOLUCAO OFICIAL (use como referencia, NAO copie): ").append(question.getSolution()).append("\n");
        }
        prompt.append("3. Graficos: Se houver graficos, a solution contem a analise detalhada\n");
        prompt.append("4. Imagem: Se houver imagem, a solution descreve o que ela contem\n\n");

        prompt.append("ESTRATEGIA PEDAGOGICA:\n");
        prompt.append("- Voce tem a solucao oficial, use para guiar o aluno\n");
        prompt.append("- Faca perguntas progressivas que levem a resposta\n");
        prompt.append("- Confirme acertos, corrija erros gentilmente\n");
        prompt.append("- NUNCA dependa do aluno para descrever graficos ou imagens\n\n");

        // =========================================================
        // GRAFICOS E IMAGENS
        // =========================================================
        if (hasGraph) {
            prompt.append("INFORMACAO SOBRE GRAFICOS:\n");
            prompt.append("- Esta questao contem GRAFICOS que o aluno ve na tela\n");
            prompt.append("- Use a SOLUTION para interpretar os graficos\n");
            prompt.append("- NUNCA mencione expressoes matematicas originais\n");
            prompt.append("- Exemplo: Observando o grafico, a curva intersecta o eixo x em...\n\n");
        }

        if (hasImage) {
            prompt.append("INFORMACAO SOBRE IMAGEM:\n");
            prompt.append("- Esta questao contem uma imagem que o aluno ve na tela\n");
            prompt.append("- Use a SOLUTION para saber o que a imagem mostra\n");
            prompt.append("- Exemplo: Na figura, vemos um triangulo retangulo com catetos...\n\n");
        }

        // =========================================================
        // FORMATAÇÃO LaTeX COMPLETA
        // =========================================================
        prompt.append("REGRAS DE FORMATACAO LaTeX (OBRIGATORIAS):\n\n");

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
        prompt.append("\\]\n");
        prompt.append("Onde: o numero a esquerda (2) e a raiz, a primeira linha sao os coeficientes, a ultima linha e o quociente e resto\n\n");

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

        prompt.append("FORMATACAO GERAL:\n");
        prompt.append("- Use **negrito** para conceitos importantes\n");
        prompt.append("- Use *italico* para enfase\n");
        prompt.append("- Use `codigo` para formulas ou comandos\n");
        prompt.append("- Use listas numeradas para passos sequenciais:\n");
        prompt.append("  1. Primeiro passo\n");
        prompt.append("  2. Segundo passo\n");
        prompt.append("- Use marcadores para itens relacionados:\n");
        prompt.append("  * Item 1\n");
        prompt.append("  * Item 2\n\n");

        prompt.append("IMPORTANTE:\n");
        prompt.append("- NUNCA use $ ou $$ - use apenas \\( \\) e \\[ \\]\n");
        prompt.append("- SEMPRE verifique a formatacao LaTeX antes de responder\n\n");

        // =========================================================
        // CONTEUDO DA QUESTAO
        // =========================================================
        prompt.append("TOPICO: ").append(topicName).append("\n\n");
        prompt.append("QUESTAO:\n").append(question.getText()).append("\n\n");

        prompt.append("ALTERNATIVAS:\n");
        for (Answer answer : question.getAnswers()) {
            prompt.append("- ").append(answer.getText()).append("\n");
        }
        prompt.append("\n");

        // =========================================================
        // RESPOSTA DO ALUNO
        // =========================================================
        if (selectedAnswer != null) {
            prompt.append("RESPOSTA DO ALUNO: ").append(selectedAnswer.getText()).append("\n");
            if (correctAnswer != null) {
                boolean isCorrect = selectedAnswer.getId().equals(correctAnswer.getId());
                prompt.append("(Status interno: ").append(isCorrect ? "CORRETA" : "INCORRETA").append(" - use para guiar, NAO revele)\n\n");
            }
        }

        // =========================================================
        // HISTORICO
        // =========================================================
        if (!history.isEmpty()) {
            prompt.append("HISTORICO DA CONVERSA:\n");
            for (TutorMessage msg : history) {
                String role = msg.getRole() == MessageRole.USER ? firstName : "TUTOR";
                String content = msg.getContent();
                if (content != null && content.length() > 300) {
                    content = content.substring(0, 300) + "...";
                }
                prompt.append(role).append(": ").append(content).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("PERGUNTA DO ALUNO: ").append(userMessage).append("\n\n");

        prompt.append("INSTRUCOES FINAIS:\n");
        prompt.append("1. Ajude o aluno a RESOLVER o exercicio, nao de a resposta\n");
        prompt.append("2. Use a solucao oficial como guia\n");
        prompt.append("3. Faca perguntas que estimulem o raciocinio\n");
        prompt.append("4. Seja paciente e encorajador\n");
        prompt.append("5. Use formatacao LaTeX correta (\\( \\) para inline, \\[ \\] para destaques)\n");
        prompt.append("6. NUNCA use $ ou $$ - use apenas \\( \\) e \\[ \\]\n");

        return prompt.toString();
    }
}