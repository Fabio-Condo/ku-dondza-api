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

@Service
public class TutorAiService {

    private static final Logger log = LoggerFactory.getLogger(TutorAiService.class);

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
        if (user == null) {
            throw new UserNotFoundException("Usuário não encontrado");
        }

        Question question = questionService.findById(request.getQuestionId());
        if (question == null) {
            throw new QuestionNotFoundException("Questão não encontrada");
        }

        TutorConversation conversation =
                conversationService.getOrCreate(request.getUserId(), question);

        // Detect intent using AI
        TutorIntent intent = detectIntentWithAI(request.getMessage(), question);
        log.info("Usuário {} - Questão {} - Intent detectada: {}",
                request.getUserId(), request.getQuestionId(), intent);

        // =====================================================
        // OUT OF SCOPE (bloqueio inteligente)
        // =====================================================
        if (intent == TutorIntent.OUT_OF_SCOPE) {

            String response = String.format(
                    "Olá %s! Posso ajudar apenas com dúvidas relacionadas a esta questão ou aos conceitos matemáticos envolvidos. " +
                            "Vamos focar na matemática? 😊",
                    user.getFullName()
            );

            messageService.saveUserMessage(conversation, request.getMessage());
            messageService.saveAssistantMessage(conversation, response);

            conversation.setUpdatedAt(LocalDateTime.now());

            log.info("Out of scope detectado para usuário {}", request.getUserId());
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
                        12
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
                user  // Pass user object to prompt builder
        );

        // Log prompt for debugging (optional, can be removed in production)
        if (log.isDebugEnabled()) {
            log.debug("Prompt enviado ao GPT: {}", prompt.substring(0, Math.min(500, prompt.length())));
        }

        String aiResponse = gptService.askAssistant(prompt);

        messageService.saveAssistantMessage(conversation, aiResponse);
        conversation.setUpdatedAt(LocalDateTime.now());

        return aiResponse;
    }

    // =========================================================
    // INTENT DETECTOR VIA IA
    // =========================================================
    private TutorIntent detectIntentWithAI(String message, Question question) {

        // Handle empty message
        if (message == null || message.trim().isEmpty()) {
            return TutorIntent.HINT;
        }

        // Prepare a short version of the question (first 200 chars)
        String questionShort = question.getText().length() > 200
                ? question.getText().substring(0, 200) + "..."
                : question.getText();

        String classificationPrompt = String.format(
                "Você é um classificador de intenções para um tutor de matemática.\n" +
                        "Classifique a mensagem do aluno em uma das seguintes categorias:\n" +
                        "\n" +
                        "HINT - o aluno pede uma dica ou ajuda inicial (ex: \"me dá uma dica\", \"pode ajudar?\", \"como começo?\")\n" +
                        "EXPLANATION - pede explicação de um conceito matemático (ex: \"o que é logaritmo?\", \"por que isso acontece?\")\n" +
                        "STEP_BY_STEP - quer resolver passo a passo com o tutor (ex: \"vamos resolver juntos\", \"passo a passo\", \"me guia\")\n" +
                        "VERIFY_REASONING - quer que o tutor verifique um raciocínio (ex: \"acho que é assim...\", \"meu raciocínio está certo?\", \"resolvi dessa forma\")\n" +
                        "OUT_OF_SCOPE - pergunta totalmente fora da matemática ou da questão (ex: \"qual a capital do Brasil?\", \"que horas são?\", \"como está o tempo?\")\n" +
                        "\n" +
                        "Contexto da questão (apenas para referência): %s\n" +
                        "\n" +
                        "Mensagem do aluno: \"%s\"\n" +
                        "\n" +
                        "Retorne APENAS uma das palavras: HINT, EXPLANATION, STEP_BY_STEP, VERIFY_REASONING, OUT_OF_SCOPE.\n" +
                        "Não adicione nenhuma outra explicação ou texto.",
                questionShort,
                message
        );

        try {
            String aiResponse = gptService.askAssistant(classificationPrompt).trim().toUpperCase();

            // Validate if the response is a valid enum value
            for (TutorIntent intent : TutorIntent.values()) {
                if (intent.name().equals(aiResponse)) {
                    log.debug("Intenção classificada como: {}", intent);
                    return intent;
                }
            }

            // If response is not valid, fallback to EXPLANATION
            log.warn("Resposta inesperada da IA na classificação: {}, usando fallback EXPLANATION", aiResponse);
            return TutorIntent.EXPLANATION;

        } catch (Exception e) {
            log.error("Erro ao classificar intenção via IA", e);
            // Fallback seguro em caso de erro
            return TutorIntent.EXPLANATION;
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

        // ========================
        // MISSÃO
        // ========================
        prompt.append("MISSÃO:\n");
        prompt.append("Você é o Tutor AI da plataforma Dikahub.\n");
        prompt.append("Seu nome é Tutor AI e você está ajudando ").append(user.getFullName()).append(".\n");
        prompt.append("Sua única função é ajudar o aluno a resolver esta questão.\n");
        prompt.append("Não responda perguntas fora do contexto.\n\n");

        // ========================
        // INFORMAÇÕES DO ALUNO
        // ========================
        prompt.append("INFORMAÇÕES DO ALUNO:\n");
        prompt.append("Nome: ").append(user.getFullName()).append("\n");
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            prompt.append("Email: ").append(user.getEmail()).append("\n");
        }
        prompt.append("\n");

        // ========================
        // REGRAS
        // ========================
        prompt.append("REGRAS:\n");
        prompt.append("- Seja pedagógico e acolhedor\n");
        prompt.append("- Sempre que apropriado, use o nome do aluno para tornar a conversa mais pessoal\n");
        prompt.append("- Explique de forma simples e clara\n");
        prompt.append("- Não revele a resposta imediatamente\n");
        prompt.append("- Incentive o raciocínio do aluno\n");
        prompt.append("- Use exemplos quando necessário\n");
        prompt.append("- Use LaTeX quando apropriado\n");
        prompt.append("- Considere o histórico da conversa\n");
        prompt.append("- Redirecione assuntos fora do contexto matemático educadamente\n");
        prompt.append("- Seja paciente e encorajador\n");
        prompt.append("- Elogie os acertos e esforços do aluno\n\n");

        // ========================
        // CONTEXTO INTELIGENTE
        // ========================
        prompt.append("TIPO DE AJUDA SOLICITADA:\n");

        switch (intent) {
            case HINT:
                prompt.append("O aluno pediu uma DICA. Dê apenas uma dica curta e objetiva.\n");
                prompt.append("Use o nome do aluno para tornar a dica mais pessoal.\n");
                prompt.append("Exemplo: \"").append(user.getFullName()).append(", que tal começar observando...\"\n");
                prompt.append("NÃO dê a resposta completa.\n\n");
                break;

            case STEP_BY_STEP:
                prompt.append("O aluno quer resolver PASSO A PASSO com você.\n");
                prompt.append("Guie o aluno passo a passo sem revelar tudo de uma vez.\n");
                prompt.append("Use o nome do aluno e faça perguntas para estimular o raciocínio.\n");
                prompt.append("Exemplo: \"").append(user.getFullName()).append(", vamos juntos? Primeiro, o que você observa?\"\n\n");
                break;

            case VERIFY_REASONING:
                prompt.append("O aluno quer que você VERIFIQUE o raciocínio dele.\n");
                prompt.append("Analise o raciocínio apresentado pelo aluno com cuidado.\n");
                prompt.append("Use o nome do aluno ao responder.\n");
                prompt.append("Se estiver correto, confirme e explique por quê: \"").append(user.getFullName()).append(", excelente raciocínio!\"\n");
                prompt.append("Se estiver errado, mostre gentilmente onde está o erro: \"").append(user.getFullName()).append(", quase lá! Vamos revisar...\"\n");
                prompt.append("Dê dicas para corrigir sem dar a resposta pronta.\n\n");
                break;

            case EXPLANATION:
                prompt.append("O aluno pediu EXPLICAÇÃO de um conceito.\n");
                prompt.append("Explique o conceito matemático necessário de forma clara e didática.\n");
                prompt.append("Use o nome do aluno para engajar: \"").append(user.getFullName()).append(", este conceito funciona assim...\"\n");
                prompt.append("Use exemplos relacionados à questão.\n\n");
                break;

            default:
                prompt.append("Explique o conceito necessário de forma pedagógica.\n");
                prompt.append("Use o nome do aluno sempre que apropriado.\n\n");
        }

        // ========================
        // QUESTÃO
        // ========================
        prompt.append("QUESTÃO:\n")
                .append(question.getText())
                .append("\n\n");

        prompt.append("ALTERNATIVAS:\n");
        for (Answer answer : question.getAnswers()) {
            prompt.append("- ").append(answer.getText()).append("\n");
        }
        prompt.append("\n");

        // ========================
        // EXPRESSÕES
        // ========================
        if (question.getMathExpressions() != null && !question.getMathExpressions().isEmpty()) {
            prompt.append("EXPRESSÕES MATEMÁTICAS:\n");
            for (MathExpression exp : question.getMathExpressions()) {
                prompt.append("- ").append(exp.getExpression()).append("\n");
            }
            prompt.append("\n");
        }

        // ========================
        // DICA FIXA (se disponível)
        // ========================
        if (question.getTip() != null && !question.getTip().isEmpty()) {
            prompt.append("DICA DISPONÍVEL:\n").append(question.getTip()).append("\n\n");
        }

        // ========================
        // HISTÓRICO DA CONVERSA
        // ========================
        if (!history.isEmpty()) {
            prompt.append("HISTÓRICO DA CONVERSA:\n");
            prompt.append("(Últimas ").append(history.size()).append(" mensagens)\n");

            for (TutorMessage msg : history) {
                String role = msg.getRole() == MessageRole.USER ? "ALUNO" : "TUTOR";
                String content = msg.getContent();

                // Truncate very long messages but keep context
                if (content != null && content.length() > 300) {
                    content = content.substring(0, 300) + "...";
                }
                prompt.append(role).append(": ").append(content).append("\n");
            }
            prompt.append("\n");
        }

        // ========================
        // CONTEXTO DA RESPOSTA DO ALUNO
        // ========================
        if (selectedAnswer == null) {
            prompt.append("CONTEXTO: AJUDA INICIAL\n");
            prompt.append("O aluno ainda não respondeu à questão.\n");
            prompt.append("Forneça orientação sem dar a resposta.\n");
            prompt.append("Use o nome ").append(user.getFullName()).append(" para motivá-lo.\n\n");
        } else {
            prompt.append("CONTEXTO: CORREÇÃO DE RESPOSTA\n");
            prompt.append("Resposta selecionada pelo aluno: ")
                    .append(selectedAnswer.getText())
                    .append("\n\n");

            // Only show correct answer if the student explicitly wants verification
            // This prevents accidentally revealing the answer
            if (intent == TutorIntent.VERIFY_REASONING && correctAnswer != null) {
                prompt.append("(Para referência do tutor - resposta correta: ")
                        .append(correctAnswer.getText())
                        .append(")\n");
                prompt.append("Use esta informação APENAS para avaliar o raciocínio do aluno.\n");
                prompt.append("NÃO revele esta resposta diretamente ao aluno.\n");
                prompt.append("Ao responder, use o nome ").append(user.getFullName()).append(".\n\n");
            } else if (correctAnswer != null && intent != TutorIntent.VERIFY_REASONING) {
                prompt.append("Nota: O aluno ainda não pediu verificação formal.\n");
                prompt.append("NÃO revele se a resposta está certa ou errada ainda.\n");
                prompt.append("Ajude o aluno a chegar à conclusão por conta própria.\n");
                prompt.append("Use o nome ").append(user.getFullName()).append(" para encorajá-lo.\n\n");
            }
        }

        // ========================
        // PERGUNTA DO ALUNO
        // ========================
        prompt.append("PERGUNTA DO ALUNO:\n")
                .append(userMessage)
                .append("\n\n");

        // ========================
        // INSTRUÇÃO FINAL
        // ========================
        prompt.append("INSTRUÇÃO FINAL:\n");
        prompt.append("Responda de acordo com o tipo de ajuda solicitado, seguindo todas as regras acima.\n");
        prompt.append("Seja educado, paciente e didático.\n");
        prompt.append("Use o nome do aluno (").append(user.getFullName()).append(") naturalmente na conversa.\n");
        prompt.append("Mantenha um tom acolhedor e encorajador.\n");

        return prompt.toString();
    }

    // =========================================================
    // INTENT ENUM
    // =========================================================
    public enum TutorIntent {
        HINT,           // Apenas uma dica curta
        EXPLANATION,    // Explicação de conceito
        STEP_BY_STEP,   // Resolução guiada passo a passo
        VERIFY_REASONING, // Verificar raciocínio do aluno
        OUT_OF_SCOPE    // Pergunta fora do contexto matemático
    }
}