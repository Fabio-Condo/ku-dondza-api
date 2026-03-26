package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.*;
import com.fabiocondo.dto.QuestionDTO;
import com.fabiocondo.dtoMapper.QuestionMapper;
import com.fabiocondo.enumeration.DifficultyLevel;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.TopicTestRepository;
import com.fabiocondo.repository.filter.QuestionFilter;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private static final String BUCKET_NAME = "exercicios-bucket";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AmazonS3Service amazonS3Service;
    private final QuestionRepository questionRepository;
    private final TopicService topicService;

    private final TopicTestRepository topicTestRepository;

    private final QuestionMapper questionMapper; // ou onde está o domainToDTO
    private final GptService gptService;

    public QuestionService(AmazonS3Service amazonS3Service, QuestionRepository questionRepository, TopicService topicService, TopicTestRepository topicTestRepository, QuestionMapper questionMapper, GptService gptService) {
        this.amazonS3Service = amazonS3Service;
        this.questionRepository = questionRepository;
        this.topicService = topicService;
        this.topicTestRepository = topicTestRepository;
        this.questionMapper = questionMapper;
        this.gptService = gptService;
    }

    public Question findById(Long id) throws QuestionNotFoundException {
        logger.info("Getting question by id: " + id);
        return questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + id));
    }

    public Question findQuestionByQuestionId(String questionId) throws QuestionNotFoundException {
        return questionRepository.findQuestionByQuestionId(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + questionId));
    }

    public Page<Question> filter(QuestionFilter questionFilter, Pageable pageable) {
        return questionRepository.filter(questionFilter, pageable);
    }

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    // USADO PARA ADD QUESTIONS NOS TESTES DE PROGRESSO
    public List<QuestionDTO> findAllByTopicAndMarkSelected(Long topicTestId) throws TopicNotFoundException {

        // 1. Buscar Test
        Test test = topicTestRepository.findById(topicTestId)
                .orElseThrow(() -> new TopicNotFoundException("No test found by id: " + topicTestId));

        // 2. Extrair IDs das questions já associadas ao Test
        Set<Long> questionIdsInTopicTest = test.getQuestions()
                .stream()
                .map(Question::getId)
                .collect(Collectors.toSet());

        // 3. Buscar todas as questions do topic
        Set<Question> questionsByTopic =
                questionRepository.findByTopicId(test.getTopic().getId());

        // 4. Converter para DTO e marcar selected
        return questionsByTopic.stream().map(question -> {

            QuestionDTO dto = questionMapper
                    .domainToDTO(question, topicTestId);

            if (questionIdsInTopicTest.contains(question.getId())) {
                dto.setSelected(true);
            } else {
                dto.setSelected(false);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    public Set<Question> getQuestionsByTopics(Set<Long> topicIds, DifficultyLevel difficultyLevel, int limitPerTopic) {
        if (topicIds == null || topicIds.isEmpty()) {
            throw new IllegalArgumentException("O Quiz deve ter pelo menos um tópico associado.");
        } else {
            return questionRepository.findRandomQuestionsByTopicsAndDifficulty(topicIds, difficultyLevel, limitPerTopic);
        }
    }

    public Set<Question> getQuestionsByTopicId(Long topicId) {
        return questionRepository.findByTopicId(topicId);
    }

    public Question save(Question question) {
        question.setQuestionId(UUID.randomUUID().toString());
        question.getAnswers().forEach(answer -> answer.setQuestion(question));
        question.getMathExpressions().forEach(mathExpression -> mathExpression.setQuestion(question));
        logger.info("Saving question: " + question.getText());
        return questionRepository.save(question);
    }

    public Question update(Question question, Long id) throws QuestionNotFoundException {
        Question existQuestion = findById(id);

        existQuestion.getAnswers().clear();
        existQuestion.getAnswers().addAll(question.getAnswers());
        existQuestion.getAnswers().forEach(answer -> answer.setQuestion(existQuestion));

        existQuestion.getMathExpressions().clear();
        existQuestion.getMathExpressions().addAll(question.getMathExpressions());
        existQuestion.getMathExpressions().forEach(mathExpression -> mathExpression.setQuestion(existQuestion));

        BeanUtils.copyProperties(question, existQuestion, "answers", "mathExpressions");
        logger.info("Updating question: " + existQuestion.getText());
        return questionRepository.save(existQuestion);
    }

    public void delete(Long id) throws QuestionNotFoundException {
        Question existQuestion = findById(id);
        logger.info("Deleting quiz: " + existQuestion.getText());
        questionRepository.deleteById(id);
    }

    public long getTotal(){
        logger.info("Total quizzes: " + questionRepository.count());
        return questionRepository.count();
    }

    public Question updateQuestionImage(Long questionId, MultipartFile file) throws QuestionNotFoundException, IOException {
        // Adicionar funcao que diminue o tamanho da imagem

        Question question = findById(questionId);

        if (file == null || file.isEmpty()) {
            throw new IOException("The file is null or empty");
        }

        // Deleta o arquivo antigo do S3
        if (question.getFileName() != null) {
            logger.info("Deleting file: " + question.getFileName());
            amazonS3Service.deleteFile(question.getFileName(), BUCKET_NAME);
        }

        String fileKey = UUID.randomUUID() + "-" + file.getOriginalFilename();
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME, fileKey);
        question.setUrlFile(s3UploadResponse.getFileUrl());
        question.setFileName(fileKey);

        questionRepository.save(question);
        return question;
    }

    public void toggleValidated(Long id, Boolean status) throws QuestionNotFoundException {
        Question question = findById(id);
        question.setValidated(status);
        questionRepository.save(question);
    }

    public Question generateAdvancedQuestionFromAI(Long topicId, String extraRule, int numberOfOptions, String exerciseFormat) {

        logger.info("Gerando questão para Tópico ID: {}, Opções: {}", topicId, numberOfOptions);

        try {
            // --- Buscar tópico ---
            Topic topic = topicService.findById(topicId);
            if (topic == null) {
                throw new TopicNotFoundException("Tópico com ID " + topicId + " não encontrado");
            }
            String subject = topic.getSubject() != null ? topic.getSubject().getName() : "Desconhecido";
            String topicName = topic.getName() != null ? topic.getName() : "Sem nome";

            // --- Criar objeto Question ---
            Question question = new Question();
            question.setTopic(topic);
            question.setTimeLimit(60);
            question.setValidated(false);

            String exerciseInstruction = "";
            if ("Matemática".equalsIgnoreCase(subject) && exerciseFormat != null && !exerciseFormat.isEmpty()) {
                exerciseInstruction = "- Formato do exercício: " + exerciseFormat + ".\n";
            }

            // --- Construir prompt ---
            String prompt = String.format(
                    "Gere uma questão no seguinte formato JSON:\n" +
                            "{\n" +
                            "  \"text\": \"enunciado em LaTeX\",\n" +
                            "  \"tip\": \"dica em LaTeX\",\n" +
                            "  \"solution\": \"solução em LaTeX\",\n" +
                            "  \"answers\": [\n" +
                            "    {\"text\": \"resposta 1\", \"correct\": false},\n" +
                            "    {\"text\": \"resposta 2\", \"correct\": true},\n" +
                            "    {\"text\": \"resposta 3\", \"correct\": false},\n" +
                            "    {\"text\": \"resposta 4\", \"correct\": false}\n" +
                            "  ],\n" +
                            "  \"mathExpressions\": [\n" +
                            "    {\"expression\": \"x^2 + 3x + 2\", \"name\": \"f(x)\"},\n" +
                            "    {\"expression\": \"\\\\frac{1}{x}\", \"name\": \"g(x)\"}\n" +
                            "  ]\n" +
                            "}\n\n" +
                            "Regras obrigatórias:\n" +
                            "- Responda **somente** com JSON válido, nada antes ou depois.\n" +
                            "- O JSON deve ser sintaticamente válido (parseável em Java).\n" +
                            "- Todos os campos são obrigatórios.\n" +
                            "- Exactamente %d alternativas em \"answers\" (uma correta, as restantes incorrectas).\n" +
                            "- \"mathExpressions\" é **opcional** e só deve ser incluído quando o enunciado exigir interpretação gráfica ou análise visual de uma função/equação.\n" +
                            "- Para LaTeX, **não use o símbolo $**. Para conteúdo inline use **\\\\( ... \\\\)** e para bloco use **\\\\[ ... \\\\]**.\n" +
                            "- Tema: %s — %s\n" +
                            "- Para todo LaTeX (enunciado, dica, solução, respostas e expressões), **não use o símbolo $**.\n" +
                            "  - Para conteúdo inline, use exactamente \\\\(...\\\\).\n" +
                            "  - Para conteúdo em bloco, use exactamente \\\\[...\\\\].\n" +
                            "- Para colocar em **negrito**, coloque o texto entre dois asteriscos. Exemplo: **Texto em negrito**.\n" +
                            "- Para colocar em *itálico*, coloque o texto entre um asterisco. Exemplo: *texto em italico*.\n" +
                            "- Para saltar linha/começar um novo parágrafo, utilize **Enter** (linha em branco). Não use \"\\n\".\n" +
                            "- Valores monetários: se mencionar dinheiro, represente-o **apenas em metical (MT)**.\n" +
                            "- Se adicionar expressões matemáticas em \"mathExpressions\":\n" +
                            "  - As variáveis devem ser **apenas em função de x**.\n" +
                            "  - Não utilize notação de função como f(x)=2x+5, escreva apenas **2x+5**.\n" +
                            "  - As expressões devem ser **não redundantes**: evite formas diferentes da mesma função. \n" +
                            "    Exemplo inválido: -2x^2+12x-10 e -2(x-3)^2+8 (são a mesma função em formas diferentes).\n" +
                            "- A questão deve ser **inteligente e não trivial**:\n" +
                            "  - Exija raciocínio do aluno, não mera memorização.\n" +
                            "  - Garanta que as alternativas incorrectas sejam **plausíveis** (não óbvias).\n" +
                            "  - O enunciado deve contextualizar bem o problema.\n" +
                            "  - A solução deve explicar o raciocínio passo a passo.\n" +
                            "- Para intervalos (ex: [0,1], ]0,1[, ]0,1], [0,1[), utilize **apenas colchetes [ ]**, inclusive para intervalos abertos. Nunca use parênteses ().\n" +
                            "%s" + // <-- instrução do formato do exercício
                            "%s", // <-- extra rule
                    numberOfOptions,
                    subject, topicName, exerciseInstruction,
                    (extraRule != null && !extraRule.isEmpty()) ? "- Regra adicional: " + extraRule : ""
            );

            System.out.println("Prompt: " + prompt);

            // --- Chamar GPT ---
            String aiResponse = gptService.askAssistant(prompt);
            if (aiResponse == null || aiResponse.isEmpty()) {
                throw new RuntimeException("Resposta da IA veio nula ou vazia");
            }

            logger.debug("Resposta bruta da IA: {}", aiResponse);
            System.out.println("Resposta bruta da IA: " + aiResponse);

            aiResponse = extractJson(aiResponse);
            logger.debug("JSON extraído: {}", aiResponse);
            System.out.println("JSON extraído: " + aiResponse);

            // --- Parse JSON ---
            ObjectMapper mapper = JsonMapper.builder()
                    .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                    .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                    .build();

            JsonNode root = mapper.readTree(aiResponse);

            question.setText(root.has("text") ? root.get("text").asText() : "Enunciado não fornecido");
            question.setTip(root.has("tip") ? root.get("tip").asText() : "Sem dica disponível");
            question.setSolution(root.has("solution") ? root.get("solution").asText() : "Sem solução disponível");

            // --- Processar respostas ---
            List<Answer> answers = new ArrayList<>();
            if (root.has("answers")) {
                for (JsonNode ansNode : root.withArray("answers")) {
                    if (!ansNode.has("text") || !ansNode.has("correct")) continue;

                    Answer ans = new Answer();
                    ans.setText(ansNode.get("text").asText());
                    ans.setCorrect(ansNode.get("correct").asBoolean());
                    ans.setQuestion(question);
                    answers.add(ans);
                }
            }

            if (answers.isEmpty()) {
                Answer defaultAns = new Answer();
                defaultAns.setText("Resposta padrão");
                defaultAns.setCorrect(true);
                defaultAns.setQuestion(question);
                answers.add(defaultAns);
            }

            long correctCount = answers.stream().filter(Answer::isCorrect).count();
            if (correctCount != 1) {
                answers.get(0).setCorrect(true);
                for (int i = 1; i < answers.size(); i++) {
                    answers.get(i).setCorrect(false);
                }
            }
            question.setAnswers(answers);

            // --- Processar expressões matemáticas (opcional) ---
            if (root.has("mathExpressions")) {
                List<MathExpression> expressions = new ArrayList<>();
                for (JsonNode expNode : root.withArray("mathExpressions")) {
                    if (expNode.has("expression")) {
                        MathExpression expr = new MathExpression();
                        expr.setExpression(expNode.get("expression").asText());

                        // --- Nova linha: define name ---
                        if (expNode.has("name")) {
                            expr.setName(expNode.get("name").asText());
                        } else {
                            expr.setName("f(x)"); // valor padrão caso não venha da IA
                        }

                        expr.setQuestion(question);
                        expressions.add(expr);
                    }
                }
                question.setMathExpressions(expressions);
            }

            return generatedQuestion(question);

        } catch (Exception e) {
            logger.error("Erro ao gerar questão pela IA", e);
            throw new RuntimeException("Erro ao gerar questão pela IA", e);
        }
    }

    public Question generatedQuestion(Question question) {
        question.setQuestionId(UUID.randomUUID().toString());

        if (question.getAnswers() != null) {
            question.getAnswers().forEach(answer -> answer.setQuestion(question));
        } else {
            question.setAnswers(new ArrayList<>());
        }

        if (question.getMathExpressions() != null) {
            question.getMathExpressions().forEach(expr -> expr.setQuestion(question));
        } else {
            question.setMathExpressions(new ArrayList<>());
        }

        logger.info("Generating question: {}", question.getText());
        return question;
    }

    private String extractJson(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // Verifica se existe choices
            if (!root.has("choices") || root.get("choices").isEmpty()) {
                throw new RuntimeException("Resposta da IA não contém choices");
            }

            // Pega o content da primeira escolha
            JsonNode contentNode = root.get("choices").get(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.asText().isEmpty()) {
                throw new RuntimeException("Resposta da IA não contém conteúdo válido");
            }

            return contentNode.asText();

        } catch (Exception e) {
            throw new RuntimeException("Falha ao extrair JSON da resposta da IA", e);
        }
    }

    // TODO: Criar um metodo de validacao e identificacao de erros

}
