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
import org.springframework.util.StringUtils;

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

    // Pattern para detectar extensões de imagem na URL
    private static final Pattern IMAGE_EXTENSION_PATTERN = Pattern.compile(
            "(?i)\\.(jpg|jpeg|png|gif|bmp|svg|webp)(\\?|$)"
    );

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
    // HELPER: Check if question has image (based on urlFile)
    // =========================================================
    private boolean hasImage(Question question) {
        if (question.getUrlFile() == null || question.getUrlFile().trim().isEmpty()) {
            return false;
        }
        String urlFile = question.getUrlFile().trim();
        // Verifica se a URL parece ser de uma imagem
        return IMAGE_EXTENSION_PATTERN.matcher(urlFile).find() ||
                urlFile.contains("image") ||
                urlFile.contains("img") ||
                urlFile.contains("upload");
    }

    // =========================================================
    // HELPER: Get image type description based on question text
    // =========================================================
    private String getImageTypeDescription(Question question) {
        String text = question.getText().toLowerCase();
        if (text.contains("triângulo") || text.contains("triangulo")) return "figura geométrica (triângulo)";
        if (text.contains("quadrado")) return "figura geométrica (quadrado)";
        if (text.contains("círculo") || text.contains("circulo")) return "figura geométrica (círculo)";
        if (text.contains("retângulo") || text.contains("retangulo")) return "figura geométrica (retângulo)";
        if (text.contains("polígono") || text.contains("poligono")) return "figura geométrica (polígono)";
        if (text.contains("gráfico") || text.contains("grafico")) return "gráfico";
        if (text.contains("desenho")) return "ilustração";
        if (text.contains("figura")) return "figura";
        if (text.contains("esquema")) return "esquema";
        if (text.contains("diagrama")) return "diagrama";
        return "imagem";
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

        // Acknowledgment (confirmações simples)
        if (msg.matches("^(ok|esta bem|ta bem|tá bem|entendi|compreendi|percebi|sei|aham|hum|sim|claro|certo|certo|beleza|show|perfeito|excelente|maravilha|blz|boto|saquei|entendido).*") ||
                containsWord(rawMsg, "esta bem", "tá bem", "ta bem", "tah bem", "ok", "blz", "beleza")) {
            return TutorIntent.ACKNOWLEDGMENT;
        }

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
        if (containsWord(rawMsg, "thanks", "tks", "obrigado", "obrigada", "valeu", "agradeço", "muito obrigado", "brigado", "brigada", "vlw", "obg", "obgd")) {
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
        boolean hasMathExpressions = question.getMathExpressions() != null && !question.getMathExpressions().isEmpty();
        boolean hasImage = hasImage(question);

        log.info("Usuário {} - Questão {} (Tópico: {}) - Intent: {} - Tem expressões: {} - Tem imagem: {}",
                request.getUserId(), request.getQuestionId(), topicName, intent, hasMathExpressions, hasImage);

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
        // ACKNOWLEDGMENT (confirmações simples como "ok", "entendi")
        // =====================================================
        if (intent == TutorIntent.ACKNOWLEDGMENT) {
            String response = buildAcknowledgmentResponse(getFirstName(user), topicName);
            messageService.saveUserMessage(conversation, request.getMessage());
            messageService.saveAssistantMessage(conversation, response);
            conversation.setUpdatedAt(LocalDateTime.now());
            log.info("Acknowledgment para usuário {}", request.getUserId());
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
                topicName,
                hasMathExpressions,
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

    // =========================================================
    // ACKNOWLEDGMENT RESPONSE BUILDER
    // =========================================================
    private String buildAcknowledgmentResponse(String firstName, String topicName) {
        String[] responses = {
                String.format("Que bom, %s! Continue assim. Tem mais alguma dúvida sobre **%s**? 😊", firstName, topicName),
                String.format("Ótimo! Estou aqui para ajudar com **%s**. Qual o próximo passo? 📚", firstName, topicName),
                String.format("Fico feliz que entendeu, %s! Precisando, estou aqui para ajudar com **%s**. 🎯", firstName, topicName),
                String.format("Perfeito! Vamos continuar então. Alguma outra dúvida sobre **%s**? 💪", firstName, topicName),
                String.format("Bom saber, %s! Se precisar de mais ajuda com **%s**, é só chamar. 🚀", firstName, topicName)
        };
        return responses[(int) (Math.random() * responses.length)];
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
    // PROMPT BUILDER (com regras para imagens, solution e tip)
    // =========================================================
    private String buildPrompt(
            Question question,
            Answer selectedAnswer,
            Answer correctAnswer,
            String userMessage,
            List<TutorMessage> history,
            TutorIntent intent,
            User user,
            String topicName,
            boolean hasMathExpressions,
            boolean hasImage) {

        StringBuilder prompt = new StringBuilder();
        String firstName = getFirstName(user);

        prompt.append("Você é o Tutor AI da plataforma Dikahub.\n");
        prompt.append("Está ajudando ").append(firstName).append(" com uma questão sobre **").append(topicName).append("**.\n\n");

        prompt.append("REGRAS GERAIS:\n");
        prompt.append("- Seja pedagógico, acolhedor e paciente\n");
        prompt.append("- Use o nome ").append(firstName).append(" na conversa\n");
        prompt.append("- Não dê a resposta pronta, estimule o raciocínio\n");
        prompt.append("- Se o aluno perguntar algo fora do tópico, redirecione educadamente\n\n");

        // =====================================================
        // REGRAS ESPECÍFICAS PARA QUESTÕES COM IMAGENS (urlFile)
        // =====================================================
        if (hasImage) {
            String imageType = getImageTypeDescription(question);
            prompt.append("🖼️ REGRAS ESPECIAIS PARA ESTA QUESTÃO (IMAGEM/ILUSTRAÇÃO):\n");
            prompt.append("- Esta questão contém uma ").append(imageType).append("\n");
            prompt.append("- O aluno pode visualizar esta imagem na tela através da URL: ").append(question.getUrlFile()).append("\n");
            prompt.append("- Você (tutor) NÃO tem acesso visual a esta imagem\n");
            prompt.append("- Você deve ajudar o aluno baseado na DESCRIÇÃO que ele fizer da imagem\n");
            prompt.append("- Peça para o aluno DESCREVER o que ele está vendo na ").append(imageType).append("\n");
            prompt.append("- Faça perguntas como: \"O que você observa na figura?\", \"Como são os ângulos?\", \"Quais medidas estão indicadas?\"\n");
            prompt.append("- Para figuras geométricas, pergunte sobre: lados, ângulos, vértices, diagonais, simetrias\n");
            prompt.append("- Para gráficos, pergunte sobre: formato da curva, pontos de intersecção, tendências\n");
            prompt.append("- Para diagramas, pergunte sobre: componentes, relações, fluxos\n");
            prompt.append("- NUNCA assuma características da imagem que o aluno não descreveu\n");
            prompt.append("- Incentive o aluno a ser detalhista na descrição visual\n\n");
        }

        // =====================================================
        // REGRAS ESPECÍFICAS PARA QUESTÕES COM GRÁFICOS (expressões)
        // =====================================================
        if (hasMathExpressions) {
            prompt.append("📈 REGRAS ESPECIAIS PARA ESTA QUESTÃO (GRÁFICOS DE FUNÇÕES):\n");
            prompt.append("- Esta questão contém **GRÁFICOS** gerados a partir de expressões matemáticas\n");
            prompt.append("- As expressões matemáticas são usadas APENAS para gerar os gráficos\n");
            prompt.append("- O aluno NÃO vê as expressões matemáticas, apenas os gráficos\n");
            prompt.append("- Você NUNCA deve mencionar, citar ou revelar as expressões matemáticas\n");
            prompt.append("- Sua análise deve ser baseada EXCLUSIVAMENTE na interpretação visual dos gráficos\n");
            prompt.append("- Fale sobre: formato da curva, pontos de intersecção, tendências, máximos/mínimos\n");
            prompt.append("- Exemplo do que NÃO fazer: \"A função f(x) = x² + 2x - 3 tem raízes...\"\n");
            prompt.append("- Exemplo do que FAZER: \"Observando o gráfico, a curva toca o eixo x em dois pontos...\"\n");
            prompt.append("- Incentive o aluno a descrever o que ele enxerga no gráfico\n\n");
        }

        prompt.append("REGRAS DE FORMATAÇÃO (IMPORTANTE):\n");
        prompt.append("- Use **negrito** para destacar conceitos importantes\n");
        prompt.append("- Use *itálico* para ênfase ou termos estrangeiros\n");
        prompt.append("- Use `código` para expressões matemáticas, fórmulas ou comandos\n\n");

        prompt.append("- Para TABELAS, use obrigatoriamente o formato LaTeX com array:\n");
        prompt.append("  ```\n");
        prompt.append("  \\[\n");
        prompt.append("  \\begin{array}{|c|c|c|}\n");
        prompt.append("  \\hline\n");
        prompt.append("  Coluna 1 & Coluna 2 & Coluna 3 \\\\\n");
        prompt.append("  \\hline\n");
        prompt.append("  Dado 1 & Dado 2 & Dado 3 \\\\\n");
        prompt.append("  Dado 4 & Dado 5 & Dado 6 \\\\\n");
        prompt.append("  \\hline\n");
        prompt.append("  \\end{array}\n");
        prompt.append("  \\]\n");
        prompt.append("  ```\n");
        prompt.append("- Exemplo real de tabela científica:\n");
        prompt.append("  ```\n");
        prompt.append("  \\[\n");
        prompt.append("  \\begin{array}{|c|c|c|}\n");
        prompt.append("  \\hline\n");
        prompt.append("  [A]_0\\ (mol/L) & [B_2]_0\\ (mol/L) & v_0\\ (mol \\cdot L^{-1} \\cdot s^{-1}) \\\\\n");
        prompt.append("  \\hline\n");
        prompt.append("  0,10 & 0,10 & 2,53 \\times 10^{-6} \\\\\n");
        prompt.append("  0,10 & 0,20 & 5,06 \\times 10^{-6} \\\\\n");
        prompt.append("  0,20 & 0,10 & 10,01 \\times 10^{-6} \\\\\n");
        prompt.append("  \\hline\n");
        prompt.append("  \\end{array}\n");
        prompt.append("  \\]\n");
        prompt.append("  ```\n");
        prompt.append("- Sempre inclua linhas horizontais (\\hline) para separar cabeçalho e dados\n");
        prompt.append("- Use barras verticais (|) nas colunas para definir bordas\n\n");

        prompt.append("- Para DIVISÃO SINTÉTICA (Regra de Ruffini) em exercícios de matemática, use o formato:\n");
        prompt.append("  ```\n");
        prompt.append("  Coeficientes: 1, 3, -4, -12\n");
        prompt.append("  Divisão sintética por 2:\n");
        prompt.append("  \\[\n");
        prompt.append("  \\begin{array}{r|rrrr}\n");
        prompt.append("  2 & 1 & 3 & -4 & -12 \\\\\n");
        prompt.append("    &   & 2 & 10 & 12 \\\\\n");
        prompt.append("  \\hline\n");
        prompt.append("    & 1 & 5 & 6 & 0\n");
        prompt.append("  \\end{array}\n");
        prompt.append("  \\]\n");
        prompt.append("  ```\n");
        prompt.append("- Explicação do formato:\n");
        prompt.append("  * O número à esquerda (2) é a raiz ou valor que está sendo testado\n");
        prompt.append("  * A primeira linha contém os coeficientes do polinômio\n");
        prompt.append("  * A segunda linha mostra os produtos acumulados\n");
        prompt.append("  * A linha final mostra os coeficientes do quociente e o resto (último número)\n");
        prompt.append("  * Se o resto for 0, o número testado é raiz do polinômio\n\n");

        prompt.append("- Use listas numeradas para passos sequenciais:\n");
        prompt.append("  1. Primeiro passo\n");
        prompt.append("  2. Segundo passo\n");
        prompt.append("  3. Terceiro passo\n");
        prompt.append("- Use listas com marcadores (-) para itens não ordenados\n");
        prompt.append("- Use quebras de linha (linha em branco) entre parágrafos para facilitar a leitura\n");
        prompt.append("- Para equações matemáticas em linha, use $...$ ou $$...$$ para equações destacadas\n");
        prompt.append("- Para blocos de código ou fórmulas multi-linha, use ``` ```\n");
        prompt.append("- Evite respostas muito longas sem pausas (máximo 4-5 linhas por parágrafo)\n");
        prompt.append("- Use emojis com moderação para tornar a conversa mais amigável (😊, 📚, 💪, 🎯)\n");
        prompt.append("- Se for explicar um conceito complexo, use títulos com ###\n");
        prompt.append("- Sempre revise a formatação antes de responder\n\n");

        prompt.append("TIPO DE AJUDA SOLICITADA:\n");
        switch (intent) {
            case HINT:
                prompt.append("Dê apenas uma dica curta e objetiva.\n");
                if (hasImage) {
                    prompt.append("Peça para o aluno descrever um aspecto específico da imagem.\n");
                }
                if (hasMathExpressions) {
                    prompt.append("Baseie a dica na análise visual do gráfico.\n");
                }
                prompt.append("Use formatação simples, sem tabelas ou listas longas.\n\n");
                break;
            case STEP_BY_STEP:
                prompt.append("Guie o aluno passo a passo.\n");
                prompt.append("Use lista numerada para cada passo.\n");
                if (hasImage) {
                    prompt.append("Cada passo deve começar com uma pergunta sobre a imagem.\n");
                    prompt.append("Exemplo: \"1. Observe a figura. Quantos lados tem o polígono?\"\n");
                }
                if (hasMathExpressions) {
                    prompt.append("Cada passo deve ser baseado na observação do gráfico.\n");
                    prompt.append("Peça ao aluno para descrever o que ele vê no gráfico a cada etapa.\n");
                }
                prompt.append("Para exercícios matemáticos que envolvem polinômios, considere usar o formato de divisão sintética.\n");
                prompt.append("Exemplo de formato:\n");
                prompt.append("  1. Primeiro, vamos identificar...\n");
                prompt.append("  2. Em seguida, calculamos...\n");
                prompt.append("  3. Por fim, concluímos que...\n\n");
                break;
            case VERIFY_REASONING:
                prompt.append("Analise o raciocínio do aluno.\n");
                prompt.append("Use formato de diálogo, citando o raciocínio do aluno entre aspas.\n");
                if (hasImage) {
                    prompt.append("Verifique se a descrição da imagem pelo aluno está correta.\n");
                    prompt.append("Se a descrição estiver errada, peça para ele observar novamente.\n");
                }
                if (hasMathExpressions) {
                    prompt.append("Se o aluno mencionar expressões matemáticas, redirecione para a análise do gráfico.\n");
                }
                prompt.append("Se houver erro, explique usando marcadores ou lista numerada.\n\n");
                break;
            default:
                prompt.append("Explique o conceito necessário de forma clara.\n");
                if (hasImage) {
                    prompt.append("Use a imagem como referência para a explicação.\n");
                }
                if (hasMathExpressions) {
                    prompt.append("Use exemplos baseados na interpretação de gráficos.\n");
                }
                prompt.append("Use títulos e sub-títulos quando apropriado (### para seções).\n\n");
        }

        prompt.append("TÓPICO: ").append(topicName).append("\n\n");
        prompt.append("QUESTÃO:\n").append(question.getText()).append("\n\n");

        prompt.append("ALTERNATIVAS:\n");
        for (Answer answer : question.getAnswers()) {
            prompt.append("- ").append(answer.getText()).append("\n");
        }
        prompt.append("\n");

        // =====================================================
        // DICA OFICIAL (tip)
        // =====================================================
        if (question.getTip() != null && !question.getTip().trim().isEmpty()) {
            prompt.append("💡 DICA OFICIAL DA QUESTÃO (use se apropriado):\n");
            prompt.append(question.getTip()).append("\n\n");
        }

        // =====================================================
        // SOLUÇÃO OFICIAL (para referência do tutor, NÃO mostrar ao aluno)
        // =====================================================
        if (question.getSolution() != null && !question.getSolution().trim().isEmpty()) {
            prompt.append("🔒 SOLUÇÃO OFICIAL (REFERÊNCIA INTERNA - NÃO REVELAR AO ALUNO):\n");
            prompt.append(question.getSolution()).append("\n\n");
            prompt.append("Use esta solução APENAS para verificar se o raciocínio do aluno está correto.\n");
            prompt.append("NUNCA copie ou revele esta solução diretamente ao aluno.\n\n");
        }

        // =====================================================
        // INFORMAÇÕES SOBRE EXPRESSÕES (sem revelar as expressões)
        // =====================================================
        if (hasMathExpressions) {
            prompt.append("⚠️ INFORMAÇÃO SOBRE GRÁFICOS:\n");
            prompt.append("Esta questão contém gráficos gerados a partir de expressões matemáticas.\n");
            prompt.append("O aluno NÃO tem acesso às expressões, apenas aos gráficos.\n");
            prompt.append("Você NUNCA deve mencionar as expressões em suas respostas.\n");
            prompt.append("Baseie sua análise APENAS na interpretação visual do gráfico.\n\n");

            prompt.append("TIPOS DE GRÁFICOS DISPONÍVEIS PARA O ALUNO:\n");
            for (MathExpression exp : question.getMathExpressions()) {
                String expr = exp.getExpression();
                if (expr.contains("=") || expr.contains("x") || expr.contains("y")) {
                    prompt.append("- Gráfico de função (curva no plano cartesiano)\n");
                } else {
                    prompt.append("- Gráfico de ").append(expr.substring(0, Math.min(30, expr.length()))).append("\n");
                }
            }
            prompt.append("(O aluno vê esses gráficos visualmente, não as expressões)\n\n");
        }

        // =====================================================
        // INFORMAÇÕES SOBRE IMAGEM (via urlFile)
        // =====================================================
        if (hasImage) {
            String imageType = getImageTypeDescription(question);
            prompt.append("🖼️ INFORMAÇÃO SOBRE IMAGEM:\n");
            prompt.append("Esta questão contém uma ").append(imageType).append("\n");
            prompt.append("URL da imagem: ").append(question.getUrlFile()).append("\n");
            prompt.append("O aluno pode visualizar esta imagem na tela.\n");
            prompt.append("Você NÃO tem acesso visual a esta imagem.\n");
            prompt.append("Confie na descrição que o aluno fizer da imagem.\n");
            prompt.append("Faça perguntas para ajudá-lo a observar detalhes importantes.\n");
            prompt.append("Exemplo de pergunta: \"O que você pode me dizer sobre a forma que está desenhada?\"\n\n");
        }

        if (selectedAnswer != null) {
            prompt.append("RESPOSTA DO ALUNO: ").append(selectedAnswer.getText()).append("\n\n");
            if (intent == TutorIntent.VERIFY_REASONING && correctAnswer != null) {
                prompt.append("(Referência interna - resposta correta: ").append(correctAnswer.getText()).append(")\n");
                prompt.append("NÃO revele esta resposta ao aluno. Use apenas para avaliar.\n\n");
            }
        }

        if (!history.isEmpty()) {
            prompt.append("HISTÓRICO DA CONVERSA:\n");
            for (TutorMessage msg : history.subList(Math.max(0, history.size() - 6), history.size())) {
                String role = msg.getRole() == MessageRole.USER ? firstName : "TUTOR";
                String content = msg.getContent();
                if (content != null && content.length() > 200) {
                    content = content.substring(0, 200) + "...";
                }
                prompt.append(role).append(": ").append(content).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("PERGUNTA DO ALUNO: ").append(userMessage).append("\n\n");

        prompt.append("INSTRUÇÕES FINAIS:\n");
        prompt.append("1. Responda de forma educada e didática\n");
        prompt.append("2. Use o formato LaTeX com array para TODAS as tabelas\n");
        prompt.append("3. Para divisão sintética (Ruffini), use o formato mostrado com array r|rrrr\n");
        prompt.append("4. Sempre inclua \\hline para linhas horizontais nas tabelas\n");

        if (hasImage) {
            prompt.append("5. Peça para o aluno DESCREVER a imagem antes de tentar resolver\n");
            prompt.append("6. Faça perguntas específicas sobre a figura geométrica ou ilustração\n");
            prompt.append("7. Lembre-se: você NÃO vê a imagem, apenas o aluno\n");
        }

        if (hasMathExpressions) {
            prompt.append("8. NUNCA mencione expressões matemáticas - fale apenas sobre os GRÁFICOS\n");
            prompt.append("9. O aluno só vê os gráficos, não as funções que os geraram\n");
        }

        if (question.getSolution() != null && !question.getSolution().trim().isEmpty()) {
            prompt.append("10. Use a solução oficial APENAS como referência para avaliar o aluno\n");
            prompt.append("11. NUNCA copie ou revele a solução oficial diretamente\n");
        }

        prompt.append("12. Mantenha o foco no tópico: **").append(topicName).append("**\n");
        prompt.append("13. Revise a formatação antes de enviar a resposta\n");

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
        ACKNOWLEDGMENT,
        HINT,
        EXPLANATION,
        STEP_BY_STEP,
        VERIFY_REASONING,
        OUT_OF_SCOPE,
        UNCLEAR
    }
}