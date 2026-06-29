package com.fabiocondo.tutor_ai.message;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.GptService;
import com.fabiocondo.service.impl.SubjectServiceImpl;
import com.fabiocondo.service.impl.TopicService;
import com.fabiocondo.service.impl.UserServiceImpl;
import com.fabiocondo.tutor_ai.TutorRequest;
import com.fabiocondo.tutor_ai.conversation.TutorConversation;
import com.fabiocondo.tutor_ai.conversation.TutorConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TutorGeneralService {

    private static final Logger log = LoggerFactory.getLogger(TutorGeneralService.class);

    private final SubjectServiceImpl subjectService;
    private final TopicService topicService;
    private final GptService gptService;
    private final TutorConversationService conversationService;
    private final TutorMessageService messageService;
    private final UserServiceImpl userService;

    // Contadores de tokens para estatísticas
    private long totalPromptTokens = 0;
    private long totalResponseTokens = 0;
    private long totalRequests = 0;

    public TutorGeneralService(
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

    private String sanitizeText(String text) {
        if (text == null) return "";
        String sanitized = Normalizer.normalize(text, Normalizer.Form.NFC);
        sanitized = sanitized.replaceAll("[^\\x00-\\x7F\\p{L}\\p{N}\\p{P}\\p{Z}]", "");
        return sanitized;
    }

    // Estima o número de tokens (aproximadamente 4 caracteres por token)
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

        Subject subject = subjectService.findById(request.getSubjectId());
        if (subject == null) {
            throw new SubjectNotFoundException("Disciplina nao encontrada");
        }

        String subjectName = subject.getName();
        String firstName = getFirstName(user);

        log.info("[SUBJECT] =========================================");
        log.info("[SUBJECT] Iniciando requisicao - Usuario: {} - Disciplina: {}", request.getUserId(), subjectName);
        log.info("[SUBJECT] Mensagem do aluno: {}", request.getMessage() != null ? request.getMessage().substring(0, Math.min(100, request.getMessage().length())) : "(vazia)");

        TutorConversation subjectConversation =
                conversationService.getOrCreateForSubject(request.getUserId(), subject);

        if (request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
            messageService.saveUserMessage(subjectConversation, request.getMessage());
        }

        List<TutorMessage> history = conversationService.getLastSubjectMessages(
                request.getUserId(),
                request.getSubjectId(),
                15
        );

        log.info("[SUBJECT] Historico carregado - {} mensagens", history.size());

        List<Topic> subjectTopics = topicService.getBySubjectId(subject.getId());

        String prompt = buildPrompt(
                subject,
                subjectTopics,
                request.getMessage(),
                history,
                user,
                subjectName
        );

        String sanitizedPrompt = sanitizeText(prompt);

        // Estatísticas do prompt
        int promptLength = sanitizedPrompt.length();
        int estimatedPromptTokens = estimateTokens(sanitizedPrompt);

        log.info("[SUBJECT] PROMPT: tamanho = {} caracteres, tokens estimados = {}", promptLength, estimatedPromptTokens);

        if (log.isDebugEnabled()) {
            log.debug("[SUBJECT] Prompt completo (primeiros 500 caracteres): {}",
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

        log.info("[SUBJECT] RESPOSTA: tamanho = {} caracteres, tokens estimados = {}", responseLength, estimatedResponseTokens);
        log.info("[SUBJECT] Tempo de resposta: {} ms", responseTime);
        log.info("[SUBJECT] =========================================");
        log.info("[SUBJECT] ESTATISTICAS ACUMULADAS:");
        log.info("[SUBJECT] - Total de requests: {}", totalRequests);
        log.info("[SUBJECT] - Total de tokens enviados (prompts): ~{}", totalPromptTokens);
        log.info("[SUBJECT] - Total de tokens recebidos (respostas): ~{}", totalResponseTokens);
        log.info("[SUBJECT] - Total de tokens (prompt + resposta): ~{}", totalPromptTokens + totalResponseTokens);
        log.info("[SUBJECT] - Media de tokens por request: ~{}", (totalPromptTokens + totalResponseTokens) / totalRequests);
        log.info("[SUBJECT] =========================================\n");

        messageService.saveAssistantMessage(subjectConversation, sanitizedResponse);
        subjectConversation.setUpdatedAt(LocalDateTime.now());

        return sanitizedResponse;
    }

    private String buildPrompt(
            Subject subject,
            List<Topic> topics,
            String userMessage,
            List<TutorMessage> history,
            User user,
            String subjectName) {

        StringBuilder prompt = new StringBuilder();
        String firstName = getFirstName(user);

        prompt.append("Voce e o Tutor Geral da Disciplina ").append(subjectName).append(" na plataforma Dikahub.\n");
        prompt.append("Esta conversando com ").append(firstName).append(".\n\n");

        prompt.append("ESTRUTURA DA PLATAFORMA:\n");
        prompt.append("- Cada Curso/Disciplina (ex: Matematica) tem um Tutor Geral (voce)\n");
        prompt.append("- Dentro de cada disciplina, existem Modulos/Topicos\n");
        prompt.append("- Cada Modulo/Topico tem seu proprio Tutor Especialista\n");
        prompt.append("- Os modulos contem: videoaulas, PDFs, exercicios e um Tutor AI especialista\n\n");

        prompt.append("LOCALIZACAO DO TUTOR ESPECIALISTA NA PLATAFORMA:\n");
        prompt.append("Quando o aluno pedir para ser conectado ou perguntar onde encontrar o especialista, voce DEVE fornecer instrucoes DETALHADAS:\n\n");
        prompt.append("Pelo Computador (Web/Mobile):\n");
        prompt.append("1. Acesse o curso de ").append(subjectName).append("\n");
        prompt.append("2. Na pagina do curso, localize o modulo desejado\n");
        prompt.append("3. Clique no modulo para expandir o conteudo\n");
        prompt.append("4. Role a pagina ate o final do modulo\n");
        prompt.append("5. Voce encontrara o Tutor AI Especialista com um botao 'Iniciar Conversa'\n");
        prompt.append("6. Clique para comecar a conversar\n\n");

        prompt.append("TOPICOS DISPONIVEIS NESTA DISCIPLINA COM SEUS TUTORES ESPECIALISTAS:\n");
        for (Topic topic : topics) {
            prompt.append("- ").append(topic.getName());
            if (topic.getDescription() != null && !topic.getDescription().isEmpty()) {
                String desc = topic.getDescription();
                if (desc.length() > 80) {
                    desc = desc.substring(0, 80) + "...";
                }
                prompt.append(": ").append(desc);
            }
            prompt.append("\n");
        }
        prompt.append("\n");

        prompt.append("SEU PAPEL:\n");
        prompt.append("- Seja acolhedor, paciente e entusiasta\n");
        prompt.append("- Use o nome ").append(firstName).append(" para personalizar a conversa\n");
        prompt.append("- Responda perguntas gerais sobre a disciplina e sua estrutura\n");
        prompt.append("- IDENTIFIQUE qual topico/modulo especifico a pergunta se refere\n");
        prompt.append("- INTERPRETE a intencao do aluno pelo CONTEXTO da conversa\n\n");

        prompt.append("COMO RESPONDER A PEDIDOS DE CONEXAO OU LOCALIZACAO:\n");
        prompt.append("Quando o aluno pedir para ser CONECTADO ou perguntar ONDE encontrar o especialista, voce DEVE:\n\n");
        prompt.append("1. IDENTIFICAR o topico mencionado (ex: limites, derivadas)\n");
        prompt.append("2. RESPONDER com entusiasmo confirmando o redirecionamento\n");
        prompt.append("3. FORNECER A LOCALIZACAO EXATA do especialista dentro da plataforma:\n");
        prompt.append("   - Curso: ").append(subjectName).append("\n");
        prompt.append("   - Modulo: [nome do topico identificado]\n");
        prompt.append("   - Onde encontrar: Ao final do modulo, apos as videoaulas e exercicios\n");
        prompt.append("4. EXPLICAR o passo a passo de navegacao (Web e Mobile)\n");
        prompt.append("5. OFERECER ajuda adicional caso o aluno tenha dificuldade para encontrar\n\n");

        prompt.append("EXEMPLO DE RESPOSTA IDEAL (quando o aluno pede para conectar):\n");
        prompt.append("Claro, ").append(firstName).append("! Vou te ajudar a encontrar o Tutor Especialista em [Topico].\n\n");
        prompt.append("ONDE ENCONTRAR:\n");
        prompt.append("- Curso: ").append(subjectName).append("\n");
        prompt.append("- Modulo: [Nome do Topico]\n");
        prompt.append("- Localizacao exata: Ao final do modulo, apos as videoaulas e exercicios praticos\n\n");
        prompt.append("Como acessar (Web):\n");
        prompt.append("1. Acesse o curso de ").append(subjectName).append("\n");
        prompt.append("2. Clique no modulo [Nome do Topico]\n");
        prompt.append("3. Role a pagina ate o final\n");
        prompt.append("4. Procure pelo botao 'Tutor AI Especialista'\n\n");
        prompt.append("Como acessar (Mobile):\n");
        prompt.append("1. Abra o app Dikahub\n");
        prompt.append("2. Toque no curso ").append(subjectName).append("\n");
        prompt.append("3. Selecione o modulo [Nome do Topico]\n");
        prompt.append("4. Role ate a secao 'Tutor Especialista'\n\n");
        prompt.append("Dica: O especialista mantem historico de todas as suas conversas sobre [Topico]!\n\n");
        prompt.append("Vou te redirecionar agora!\n\n");

        prompt.append("EXEMPLO DE RESPOSTA PARA QUEM PERGUNTA ONDE FICA:\n");
        prompt.append("Otima pergunta, ").append(firstName).append("!\n\n");
        prompt.append("O Tutor Especialista em [Topico] esta disponivel dentro do modulo correspondente:\n\n");
        prompt.append("CAMINHO COMPLETO:\n");
        prompt.append("Meus Cursos -> ").append(subjectName).append(" -> Modulo [Topico] -> Tutor Especialista (no final do modulo)\n\n");
        prompt.append("PASSO A PASSO DETALHADO:\n");
        prompt.append("1. Acesse 'Meus Cursos' no menu principal\n");
        prompt.append("2. Clique no curso de ").append(subjectName).append("\n");
        prompt.append("3. Localize e expanda o modulo [Topico]\n");
        prompt.append("4. Navegue ate o final do conteudo do modulo\n");
        prompt.append("5. Voce vera o Tutor AI Especialista pronto para conversar!\n\n");

        // =========================================================
        // HISTORICO DA CONVERSA
        // =========================================================
        if (!history.isEmpty()) {
            prompt.append("HISTORICO DA CONVERSA (use para contexto e evitar repeticoes):\n");
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

        // =========================================================
        // FORMATAÇÃO LaTeX
        // =========================================================
        prompt.append("REGRAS DE FORMATACAO LaTeX:\n");
        prompt.append("- Use \\( \\) para formulas inline (ex: \\(E = mc^2\\))\n");
        prompt.append("- Use \\[ \\] para equacoes destacadas\n");
        prompt.append("- NUNCA use $ ou $$ - use apenas \\( \\) e \\[ \\]\n\n");

        // =========================================================
        // CONTEUDO E PERGUNTA
        // =========================================================
        prompt.append("DISCIPLINA: ").append(subjectName).append("\n");
        if (subject.getDescription() != null && !subject.getDescription().isEmpty()) {
            prompt.append("DESCRICAO: ").append(subject.getDescription()).append("\n");
        }
        prompt.append("\n");

        prompt.append("PERGUNTA DO ALUNO: ").append(userMessage).append("\n\n");

        prompt.append("INSTRUCOES FINAIS (OBRIGATORIAS):\n");
        prompt.append("1. INTERPRETE a intencao do aluno pelo CONTEXTO da conversa\n");
        prompt.append("2. Se o aluno pedir CONEXAO com especialista, forneca LOCALIZACAO DETALHADA\n");
        prompt.append("3. Se o aluno perguntar ONDE FICA, de o CAMINHO COMPLETO na plataforma\n");
        prompt.append("4. Use o nome ").append(firstName).append(" para personalizar\n");
        prompt.append("5. Seja entusiasta e acolhedor\n");
        prompt.append("6. NUNCA use $ ou $$ - use apenas \\( \\) e \\[ \\]\n");
        prompt.append("7. Mantenha o foco na disciplina ").append(subjectName).append("\n");

        return prompt.toString();
    }
}