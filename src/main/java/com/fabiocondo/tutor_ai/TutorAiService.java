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

    private static final Pattern GIBBERISH_PATTERN = Pattern.compile(
            "^(?i)(asdf|qwerty|zxcv|teste?|kkk|rsrs|h{2,}|[?]{2,}|[!]{2,}|[.]{3,})$"
    );

    private static final Pattern VERY_SHORT_PATTERN = Pattern.compile("^.{1,2}$");

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

    private String normalizeText(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replaceAll("[?¿!¡;:,.()\\[\\]{}<>]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    private boolean containsWord(String message, String... words) {
        String normalizedMsg = normalizeText(message);
        for (String word : words) {
            String normalizedWord = normalizeText(word);
            if (normalizedMsg.matches(".*\\b" + Pattern.quote(normalizedWord) + "\\b.*")) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesPattern(String message, String pattern) {
        String normalizedMsg = normalizeText(message);
        return normalizedMsg.matches(pattern);
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

    private TutorIntent detectIntentSimple(String message) {
        if (message == null || message.trim().isEmpty()) {
            return TutorIntent.HINT;
        }

        String rawMsg = message.trim();

        if (VERY_SHORT_PATTERN.matcher(rawMsg).matches()) {
            return TutorIntent.UNCLEAR;
        }

        if (GIBBERISH_PATTERN.matcher(rawMsg).matches()) {
            return TutorIntent.UNCLEAR;
        }

        String msg = normalizeText(rawMsg);

        if (msg.matches("^(ok|esta bem|ta bem|tá bem|entendi|compreendi|percebi|sei|aham|hum|sim|claro|certo|certo|beleza|show|perfeito|excelente|maravilha|blz|boto|saquei|entendido).*") ||
                containsWord(rawMsg, "esta bem", "tá bem", "ta bem", "tah bem", "ok", "blz", "beleza")) {
            return TutorIntent.ACKNOWLEDGMENT;
        }

        if (msg.matches(".*(como (esta|vc esta|voce esta|ta)|tudo bem|beleza|como vai|como anda|como estao as coisas|como funciona|como voce esta).*") ||
                containsWord(rawMsg, "como está", "como esta", "como voce esta", "como você está", "tudo bem", "beleza")) {
            return TutorIntent.HOW_ARE_YOU;
        }

        if (msg.matches("^(oi|ola|bom dia|boa tarde|boa noite|hey|hi|e ai|opa|fala|beleza|td bem|tudo bem|salve|iae|iae beleza).*") ||
                containsWord(rawMsg, "oi", "olá", "ola", "bom dia", "boa tarde", "boa noite", "e aí", "e ai")) {
            return TutorIntent.GREETING;
        }

        if (containsWord(rawMsg, "thanks", "tks", "obrigado", "obrigada", "valeu", "agradeço", "muito obrigado", "brigado", "brigada", "vlw", "obg", "obgd")) {
            return TutorIntent.THANKS;
        }

        if (containsWord(rawMsg, "você é ótimo", "voce é otimo", "bom tutor", "muito bom", "excelente", "incrível", "incrivel", "gostei da explicação", "gostei da explicacao")) {
            return TutorIntent.PRAISE;
        }

        if (containsWord(rawMsg, "passo a passo", "resolver comigo", "me guia", "me orienta", "passo a passo", "me ajuda a resolver")) {
            return TutorIntent.STEP_BY_STEP;
        }

        if (containsWord(rawMsg, "acho que", "meu raciocínio", "meu raciocinio", "está certo", "esta certo", "correto", "fiz certo", "esta correto")) {
            return TutorIntent.VERIFY_REASONING;
        }

        if (containsWord(rawMsg, "dica", "ajuda", "como começo", "como comeco", "por onde começar", "por onde comecar", "me ajuda")) {
            return TutorIntent.HINT;
        }

        if (containsWord(rawMsg, "clima", "tempo", "futebol", "notícias", "noticias", "política", "politica", "preço", "preco", "dinheiro", "comprar", "vender", "filme", "serie", "música", "musica", "jogo", "viagem", "fim de semana", "feriado")) {
            if (!containsWord(rawMsg, "questão", "questao", "exercício", "exercicio", "prova", "estudo", "matéria", "materia", "aula", "conteúdo", "conteudo")) {
                return TutorIntent.OUT_OF_SCOPE;
            }
        }

        return TutorIntent.EXPLANATION;
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

        TutorConversation conversation = conversationService.getOrCreate(request.getUserId(), question);

        TutorIntent intent = detectIntentSimple(request.getMessage());
        String topicName = getTopicName(question);
        boolean hasGraph = hasGraph(question);
        boolean hasImage = hasImage(question);

        log.info("Usuário {} - Questão {} (Tópico: {}) - Intent: {} - Gráfico: {} - Imagem: {}",
                request.getUserId(), request.getQuestionId(), topicName, intent, hasGraph, hasImage);

        if (intent == TutorIntent.UNCLEAR) {
            String response = buildUnclearResponse(getFirstName(user));
            messageService.saveUserMessage(conversation, request.getMessage());
            messageService.saveAssistantMessage(conversation, response);
            conversation.setUpdatedAt(LocalDateTime.now());
            return response;
        }

        if (intent == TutorIntent.ACKNOWLEDGMENT) {
            String response = buildAcknowledgmentResponse(getFirstName(user), topicName);
            messageService.saveUserMessage(conversation, request.getMessage());
            messageService.saveAssistantMessage(conversation, response);
            conversation.setUpdatedAt(LocalDateTime.now());
            return response;
        }

        if (intent == TutorIntent.HOW_ARE_YOU) {
            String response = buildHowAreYouResponse(getFirstName(user));
            messageService.saveUserMessage(conversation, request.getMessage());
            messageService.saveAssistantMessage(conversation, response);
            conversation.setUpdatedAt(LocalDateTime.now());
            return response;
        }

        if (intent == TutorIntent.GREETING || intent == TutorIntent.THANKS || intent == TutorIntent.PRAISE) {
            String response = buildSocialResponse(intent, getFirstName(user));
            messageService.saveUserMessage(conversation, request.getMessage());
            messageService.saveAssistantMessage(conversation, response);
            conversation.setUpdatedAt(LocalDateTime.now());
            return response;
        }

        if (intent == TutorIntent.OUT_OF_SCOPE) {
            String response = buildOutOfScopeResponse(getFirstName(user), topicName);
            messageService.saveUserMessage(conversation, request.getMessage());
            messageService.saveAssistantMessage(conversation, response);
            conversation.setUpdatedAt(LocalDateTime.now());
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

    private String buildUnclearResponse(String firstName) {
        String[] responses = {
                String.format("Desculpe, %s, não consegui entender. Pode reformular sua pergunta? 🤔", firstName),
                String.format("%s, sua pergunta ficou um pouco confusa. Você poderia explicar melhor? 😊", firstName),
                String.format("Não entendi completamente, %s. Pode dar mais detalhes? 📝", firstName),
                String.format("%s, não ficou claro o que você precisa. Pode me dizer com mais detalhes? 🎓", firstName),
        };
        return responses[(int) (Math.random() * responses.length)];
    }

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

    private String buildPrompt(
            Question question,
            Answer selectedAnswer,
            Answer correctAnswer,
            String userMessage,
            List<TutorMessage> history,
            TutorIntent intent,
            User user,
            String topicName,
            boolean hasGraph,
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

        prompt.append("FONTES DE INFORMAÇÃO DISPONÍVEIS PARA VOCÊ:\n");
        prompt.append("1. **Dica oficial (tip)**: Use para dar orientações iniciais\n");
        prompt.append("2. **Solução oficial (solution)**: Use como referência para guiar o aluno passo a passo\n");
        prompt.append("3. **Gráficos/Expressões**: Se houver gráficos, a solution contém a análise detalhada\n");
        prompt.append("4. **Imagem (urlFile)**: Se houver imagem, a solution descreve o que ela contém\n\n");

        prompt.append("ESTRATÉGIA PEDAGÓGICA:\n");
        prompt.append("- Você JÁ TEM acesso à solução detalhada da questão\n");
        prompt.append("- Use a solução para guiar o aluno, não para dar a resposta pronta\n");
        prompt.append("- Faça perguntas que levem o aluno a descobrir o caminho sozinho\n");
        prompt.append("- Confirme quando o aluno acertar, corrija gentilmente quando errar\n");
        prompt.append("- NUNCA dependa do aluno para descrever gráficos ou imagens - você já tem essa informação na solução\n\n");

        // Se tem gráficos
        if (hasGraph) {
            prompt.append("📈 INFORMAÇÃO SOBRE GRÁFICOS:\n");
            prompt.append("- Esta questão contém GRÁFICOS gerados a partir de expressões matemáticas\n");
            prompt.append("- O aluno vê os gráficos visualmente na tela\n");
            prompt.append("- Você tem a análise completa dos gráficos na SOLUTION da questão\n");
            prompt.append("- Use a SOLUTION para entender o que o gráfico mostra\n");
            prompt.append("- NUNCA mencione as expressões matemáticas originais para o aluno\n");
            prompt.append("- Exemplo de como usar: \"Observando o gráfico, podemos ver que a curva intersecta o eixo x em...\"\n\n");
        }

        // Se tem imagem
        if (hasImage) {
            prompt.append("🖼️ INFORMAÇÃO SOBRE IMAGEM:\n");
            prompt.append("- Esta questão contém uma imagem/ilustração\n");
            prompt.append("- URL da imagem: ").append(question.getUrlFile()).append("\n");
            prompt.append("- O aluno vê esta imagem na tela\n");
            prompt.append("- Você tem a descrição completa da imagem na SOLUTION da questão\n");
            prompt.append("- Use a SOLUTION para saber o que a imagem mostra (medidas, formas, etc.)\n");
            prompt.append("- Exemplo de como usar: \"Na figura, podemos ver um triângulo retângulo com catetos de 3cm e 4cm...\"\n\n");
        }

        prompt.append("REGRAS DE FORMATAÇÃO:\n");
        prompt.append("- Use **negrito** para destacar conceitos importantes\n");
        prompt.append("- Use *itálico* para ênfase\n");
        prompt.append("- Use `código` para fórmulas ou comandos\n");
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
        prompt.append("- Use listas numeradas para passos sequenciais\n");
        prompt.append("- Use quebras de linha entre parágrafos\n\n");

        prompt.append("TIPO DE AJUDA:\n");
        switch (intent) {
            case HINT:
                prompt.append("Dê apenas uma dica curta, baseada na DICA OFICIAL da questão.\n");
                prompt.append("NÃO dê a resposta completa.\n\n");
                break;
            case STEP_BY_STEP:
                prompt.append("Guie o aluno passo a passo usando a SOLUÇÃO OFICIAL.\n");
                prompt.append("Use lista numerada para cada passo.\n");
                prompt.append("Em cada passo, faça uma pergunta antes de dar a informação.\n");
                prompt.append("Exemplo: \"1. Primeiro, observe o gráfico. O que você pode dizer sobre a inclinação da reta?\"\n\n");
                break;
            case VERIFY_REASONING:
                prompt.append("Analise o raciocínio do aluno comparando com a SOLUÇÃO OFICIAL.\n");
                prompt.append("Use formato de diálogo, citando o raciocínio do aluno.\n");
                prompt.append("Se estiver correto: \"Excelente raciocínio, \" + firstName + \"! Isso está correto porque...\"\n");
                prompt.append("Se estiver errado: \"Quase lá, \" + firstName + \"! Vamos revisar este ponto...\"\n\n");
                break;
            default:
                prompt.append("Explique o conceito necessário usando a SOLUÇÃO OFICIAL como guia.\n");
                prompt.append("Use exemplos da própria questão.\n\n");
        }

        prompt.append("TÓPICO: ").append(topicName).append("\n\n");
        prompt.append("QUESTÃO:\n").append(question.getText()).append("\n\n");

        prompt.append("ALTERNATIVAS:\n");
        for (Answer answer : question.getAnswers()) {
            prompt.append("- ").append(answer.getText()).append("\n");
        }
        prompt.append("\n");

        // DICA OFICIAL
        if (question.getTip() != null && !question.getTip().trim().isEmpty()) {
            prompt.append("💡 DICA OFICIAL DA QUESTÃO:\n");
            prompt.append(question.getTip()).append("\n\n");
        }

        // SOLUÇÃO OFICIAL COMPLETA
        if (question.getSolution() != null && !question.getSolution().trim().isEmpty()) {
            prompt.append("🔬 SOLUÇÃO OFICIAL DA QUESTÃO (use como referência para guiar o aluno):\n");
            prompt.append(question.getSolution()).append("\n\n");
            prompt.append("IMPORTANTE: Você tem a solução completa. Use-a para:\n");
            prompt.append("- Entender os conceitos envolvidos\n");
            prompt.append("- Saber o que o gráfico/imagem mostra\n");
            prompt.append("- Verificar se o aluno está no caminho certo\n");
            prompt.append("- Fazer perguntas que levem o aluno à resposta\n");
            prompt.append("- NUNCA copie e cole a solução diretamente para o aluno\n\n");
        }

        if (selectedAnswer != null) {
            prompt.append("RESPOSTA SELECIONADA PELO ALUNO: ").append(selectedAnswer.getText()).append("\n\n");
            if (intent == TutorIntent.VERIFY_REASONING && correctAnswer != null) {
                boolean isCorrect = selectedAnswer.getId().equals(correctAnswer.getId());
                prompt.append("(Para sua referência - Resposta do aluno está ").append(isCorrect ? "CORRETA" : "INCORRETA").append(")\n");
                prompt.append("Resposta correta: ").append(correctAnswer.getText()).append("\n");
                prompt.append("NÃO revele esta informação diretamente ao aluno. Use para guiá-lo.\n\n");
            }
        }

        if (!history.isEmpty()) {
            prompt.append("HISTÓRICO DA CONVERSA:\n");
            int start = Math.max(0, history.size() - 6);
            for (int i = start; i < history.size(); i++) {
                TutorMessage msg = history.get(i);
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
        prompt.append("1. Use a SOLUTION OFICIAL como seu guia principal\n");
        prompt.append("2. NUNCA dependa do aluno para descrever gráficos ou imagens - você já tem essa informação\n");
        prompt.append("3. Faça perguntas que estimulem o raciocínio\n");
        prompt.append("4. Confirme acertos, corrija erros gentilmente\n");
        prompt.append("5. Use formatação adequada (LaTeX para tabelas e fórmulas)\n");
        prompt.append("6. Mantenha o foco no tópico: **").append(topicName).append("**\n");

        return prompt.toString();
    }

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