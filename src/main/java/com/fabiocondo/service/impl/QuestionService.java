package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.DifficultyLevel;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.QuizNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.filter.QuestionFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.plaf.synth.SynthEditorPaneUI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class QuestionService {

    private static final String BUCKET_NAME = "b-tests-bucket";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AmazonS3Service amazonS3Service;
    private final QuestionRepository questionRepository;
    private final TopicService topicService;
    private final GptService gptService;

    public QuestionService(AmazonS3Service amazonS3Service, QuestionRepository questionRepository, TopicService topicService, GptService gptService) {
        this.amazonS3Service = amazonS3Service;
        this.questionRepository = questionRepository;
        this.topicService = topicService;
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

    public Question generateAdvancedQuestionFromAI(Long topicId, DifficultyLevel difficulty) throws TopicNotFoundException, QuestionNotFoundException {

        Topic topic = topicService.findById(topicId);
        String subject = topic.getSubject().getName();
        String topicName = topic.getName();

        Question question = new Question();
        question.setTopic(topic);
        question.setDifficultyLevel(difficulty);
        question.setTimeLimit(60);

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
                        "    {\"expression\": \"x^2 + 3x + 2\"},\n" +
                        "    {\"expression\": \"\\\\frac{1}{x}\"}\n" +
                        "  ]\n" +
                        "}\n\n" +
                        "Regras obrigatórias:\n" +
                        "- Responda **somente** com o JSON válido, nada antes ou depois.\n" +
                        "- Não inclua explicações, comentários ou texto fora do JSON.\n" +
                        "- O JSON deve ser sintaticamente válido (parseável em Java).\n" +
                        "- Todos os campos são obrigatórios.\n" +
                        "- O enunciado, a dica e a solução devem conter apenas LaTeX válido.\n" +
                        "- Máximo de 4 alternativas em \"answers\" (uma correta, as demais incorretas).\n" +
                        "- \"mathExpressions\" é opcional, mas inclua se houver expressões relevantes.\n" +
                        "- Tema: %s — %s\n" +
                        "- Dificuldade: %s\n",
                subject, topicName, difficulty
        );

        try {
            String aiResponse = gptService.askAssistant(prompt);
            aiResponse = extractJson(aiResponse);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(aiResponse);

            question.setText(root.get("text").asText());
            question.setTip(root.get("tip").asText());
            question.setSolution(root.get("solution").asText());

            // Falta de fallback se o JSON não tiver todos os campos
            // Se a IA não gerar "tip" ou "mathExpressions", root.get("tip").asText() pode dar NPE.
            // Melhor usar path() em vez de get():
            //question.setText(root.path("text").asText(null));
            //question.setTip(root.path("tip").asText(null));
            //question.setSolution(root.path("solution").asText(null));

            List<Answer> answers = new ArrayList<>();
            for (JsonNode ansNode : root.withArray("answers")) {
                Answer ans = new Answer();
                ans.setText(ansNode.get("text").asText());
                ans.setCorrect(ansNode.get("correct").asBoolean());
                ans.setQuestion(question);
                answers.add(ans);
            }
            question.setAnswers(answers);

            if (root.has("mathExpressions")) {
                List<MathExpression> expressions = new ArrayList<>();
                for (JsonNode expNode : root.withArray("mathExpressions")) {
                    MathExpression expr = new MathExpression();
                    expr.setExpression(expNode.get("expression").asText());
                    expr.setQuestion(question);
                    expressions.add(expr);
                }
                question.setMathExpressions(expressions);
            }

            if (!validateLatex(question.getText()) || !validateLatex(question.getSolution())) {
                throw new RuntimeException("LaTeX inválido detectado");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao gerar questão pela IA: " + e.getMessage());
        }
        System.out.println("Top id: " + topicId);
        System.out.println("Difficulty: " + difficulty);

        //return findById(1L);
        return question;
    }

    private String extractJson(String response) {
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        if (start >= 0 && end >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        throw new RuntimeException("Resposta da IA não contém JSON válido");
    }

    private boolean validateLatex(String latex) {
        return latex != null && latex.contains("\\") && latex.length() > 3;
    }

    // TODO: Criar um metodo de validacao e identificacao de erros

}
