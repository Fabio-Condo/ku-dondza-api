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

    // Apenas padrões básicos para detecção inicial (evitar chamadas desnecessárias ao GPT)
    private static final Pattern IMAGE_EXTENSION_PATTERN = Pattern.compile(
            "(?i)\\.(jpg|jpeg|png|gif|bmp|svg|webp)(\\?|$)"
    );

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
        return "este tópico";
    }

    private boolean hasImage(Question question) {
        if (question.getUrlFile() == null || question.getUrlFile().trim().isEmpty()) {
            return false;
        }
        String urlFile = question.getUrlFile().trim();
        return IMAGE_EXTENSION_PATTERN.matcher(urlFile).find() ||
                urlFile.contains("image") || urlFile.contains("img") || urlFile.contains("upload");
    }

    private boolean hasGraph(Question question) {
        return question.getMathExpressions() != null && !question.getMathExpressions().isEmpty();
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

        Question question = questionService.findById(request.getQuestionId());
        if (question == null) {
            throw new QuestionNotFoundException("Questão não encontrada");
        }

        TutorConversation conversation = conversationService.getOrCreateForQuestion(request.getUserId(), question);

        String topicName = getTopicName(question);
        boolean hasGraph = hasGraph(question);
        boolean hasImage = hasImage(question);

        log.info("Usuário {} - Questão {} (Tópico: {}) - Gráfico: {} - Imagem: {}",
                request.getUserId(), request.getQuestionId(), topicName, hasGraph, hasImage);

        // Salva a mensagem do usuário
        if (request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
            messageService.saveUserMessage(conversation, request.getMessage());
        }

        // Busca histórico recente
        List<TutorMessage> history = conversationService.getLastQuestionMessages(
                request.getUserId(),
                request.getQuestionId(),
                14
        );

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

        // Constrói o prompt completo (toda a lógica fica aqui)
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

        if (log.isDebugEnabled()) {
            log.debug("Prompt enviado ao GPT: {}", prompt.substring(0, Math.min(500, prompt.length())));
        }

        String aiResponse = gptService.askAssistant(prompt);
        messageService.saveAssistantMessage(conversation, aiResponse);
        conversation.setUpdatedAt(LocalDateTime.now());

        return aiResponse;
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
        prompt.append("Você é o Tutor AI da plataforma Dikahub.\n");
        prompt.append("Está ajudando ").append(firstName).append(" com uma questão sobre **").append(topicName).append("**.\n\n");

        prompt.append("SEU PAPEL:\n");
        prompt.append("- Seja pedagógico, acolhedor e paciente\n");
        prompt.append("- Use o nome ").append(firstName).append(" naturalmente na conversa\n");
        prompt.append("- Não dê a resposta pronta, estimule o raciocínio\n");
        prompt.append("- Seja breve e objetivo (máximo 3-4 parágrafos por resposta)\n\n");

        prompt.append("INTERPRETAÇÃO DE MENSAGENS DO ALUNO:\n");
        prompt.append("Você DEVE interpretar a intenção do aluno baseado no CONTEXTO da conversa. Exemplos:\n");
        prompt.append("- Se o aluno disse apenas \"ok\", \"entendi\", \"está bem\", \"certo\", \"blz\" - isso é uma CONFIRMAÇÃO. Responda perguntando se tem mais dúvidas.\n");
        prompt.append("- Se o aluno disse \"oi\", \"olá\", \"bom dia\" - é um CUMPRIMENTO. Responda educadamente e pergunte como pode ajudar.\n");
        prompt.append("- Se o aluno disse \"obrigado\", \"valeu\" - é um AGRADECIMENTO. Responda com \"por nada\" e pergunte se precisa de mais ajuda.\n");
        prompt.append("- Se o aluno disse algo como \"como você está?\" - é uma PERGUNTA SOBRE VOCÊ. Responda que está bem e ofereça ajuda.\n");
        prompt.append("- Se o aluno disse algo fora do contexto (futebol, clima, política) - diga que só pode ajudar com **").append(topicName).append("**.\n");
        prompt.append("- Se a mensagem for muito confusa ou sem sentido - peça para reformular educadamente.\n");
        prompt.append("- Para qualquer outra mensagem, ajude com a questão normalmente.\n\n");

        // =========================================================
        // FONTES DE INFORMAÇÃO
        // =========================================================
        prompt.append("FONTES DE INFORMAÇÃO DISPONÍVEIS PARA VOCÊ:\n");
        prompt.append("1. **Dica oficial (tip)**: Use para dar orientações iniciais\n");
        prompt.append("2. **Solução oficial (solution)**: Use como referência para guiar o aluno passo a passo\n");
        prompt.append("3. **Gráficos**: Se houver gráficos, a solution contém a análise detalhada\n");
        prompt.append("4. **Imagem**: Se houver imagem, a solution descreve o que ela contém\n\n");

        prompt.append("ESTRATÉGIA PEDAGÓGICA:\n");
        prompt.append("- Você JÁ TEM acesso à solução detalhada da questão\n");
        prompt.append("- Use a solução para guiar o aluno, não para dar a resposta pronta\n");
        prompt.append("- Faça perguntas que levem o aluno a descobrir o caminho sozinho\n");
        prompt.append("- Confirme quando o aluno acertar, corrija gentilmente quando errar\n");
        prompt.append("- NUNCA dependa do aluno para descrever gráficos ou imagens - você já tem essa informação\n\n");

        // =========================================================
        // GRÁFICOS E IMAGENS
        // =========================================================
        if (hasGraph) {
            prompt.append("📈 INFORMAÇÃO SOBRE GRÁFICOS:\n");
            prompt.append("- Esta questão contém GRÁFICOS que o aluno vê na tela\n");
            prompt.append("- Você tem a análise completa dos gráficos na SOLUTION\n");
            prompt.append("- NUNCA mencione expressões matemáticas originais\n");
            prompt.append("- Exemplo: \"Observando o gráfico, podemos ver que a curva intersecta o eixo x em...\"\n\n");
        }

        if (hasImage) {
            prompt.append("🖼️ INFORMAÇÃO SOBRE IMAGEM:\n");
            prompt.append("- Esta questão contém uma imagem (URL: ").append(question.getUrlFile()).append(")\n");
            prompt.append("- O aluno vê esta imagem, você tem a descrição na SOLUTION\n");
            prompt.append("- Exemplo: \"Na figura, podemos ver um triângulo retângulo com catetos de 3cm e 4cm...\"\n\n");
        }

        // =========================================================
        // FORMATAÇÃO
        // =========================================================
        prompt.append("REGRAS DE FORMATAÇÃO:\n");
        prompt.append("- Use **negrito** para conceitos importantes\n");
        prompt.append("- Use *itálico* para ênfase\n");
        prompt.append("- Use `código` para fórmulas\n");
        prompt.append("- Para TABELAS, use LaTeX com array:\n");
        prompt.append("  \\[\n");
        prompt.append("  \\begin{array}{|c|c|c|}\n");
        prompt.append("  \\hline\n");
        prompt.append("  Coluna 1 & Coluna 2 & Coluna 3 \\\\\n");
        prompt.append("  \\hline\n");
        prompt.append("  Dado 1 & Dado 2 & Dado 3 \\\\\n");
        prompt.append("  \\hline\n");
        prompt.append("  \\end{array}\n");
        prompt.append("  \\]\n");
        prompt.append("- Para DIVISÃO SINTÉTICA (Ruffini):\n");
        prompt.append("  \\[\n");
        prompt.append("  \\begin{array}{r|rrrr}\n");
        prompt.append("  2 & 1 & 3 & -4 & -12 \\\\\n");
        prompt.append("    &   & 2 & 10 & 12 \\\\\n");
        prompt.append("  \\hline\n");
        prompt.append("    & 1 & 5 & 6 & 0\n");
        prompt.append("  \\end{array}\n");
        prompt.append("  \\]\n");
        prompt.append("- Use listas numeradas para passos sequenciais\n\n");

        // =========================================================
        // CONTEÚDO DA QUESTÃO
        // =========================================================
        prompt.append("TÓPICO: ").append(topicName).append("\n\n");
        prompt.append("QUESTÃO:\n").append(question.getText()).append("\n\n");

        prompt.append("ALTERNATIVAS:\n");
        for (Answer answer : question.getAnswers()) {
            prompt.append("- ").append(answer.getText()).append("\n");
        }
        prompt.append("\n");

        // DICA OFICIAL
        if (question.getTip() != null && !question.getTip().trim().isEmpty()) {
            prompt.append("💡 DICA OFICIAL:\n").append(question.getTip()).append("\n\n");
        }

        // SOLUÇÃO OFICIAL
        if (question.getSolution() != null && !question.getSolution().trim().isEmpty()) {
            prompt.append("🔬 SOLUÇÃO OFICIAL (use como referência, NÃO copie para o aluno):\n");
            prompt.append(question.getSolution()).append("\n\n");
        }

        // RESPOSTA DO ALUNO (se houver)
        if (selectedAnswer != null) {
            prompt.append("RESPOSTA DO ALUNO: ").append(selectedAnswer.getText()).append("\n");
            if (correctAnswer != null) {
                boolean isCorrect = selectedAnswer.getId().equals(correctAnswer.getId());
                prompt.append("(Status: ").append(isCorrect ? "CORRETA" : "INCORRETA").append(" - use para guiar, NÃO revele)\n\n");
            }
        }

        // =========================================================
        // HISTÓRICO DA CONVERSA
        // =========================================================
        if (!history.isEmpty()) {
            prompt.append("HISTÓRICO DA CONVERSA:\n");
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

        // =========================================================
        // PERGUNTA ATUAL
        // =========================================================
        prompt.append("PERGUNTA DO ALUNO: ").append(userMessage).append("\n\n");

        prompt.append("INSTRUÇÕES FINAIS:\n");
        prompt.append("1. Interprete a intenção do aluno pelo CONTEXTO da conversa\n");
        prompt.append("2. Use a SOLUTION OFICIAL como guia\n");
        prompt.append("3. NUNCA copie a solução diretamente\n");
        prompt.append("4. Responda de forma natural, como um tutor humano\n");
        prompt.append("5. Use a formatação adequada quando necessário\n");

        return prompt.toString();
    }
}