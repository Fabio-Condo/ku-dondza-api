package com.fabiocondo.tutor_ai;

import com.fabiocondo.domain.Answer;
import com.fabiocondo.domain.MathExpression;
import com.fabiocondo.domain.Question;
import com.fabiocondo.service.impl.GptService;
import com.fabiocondo.service.impl.QuestionService;
import org.springframework.stereotype.Service;

@Service
public class TutorAiService {

    private final QuestionService questionService;
    private final GptService gptService;

    public TutorAiService(QuestionService questionService,
                          GptService gptService) {
        this.questionService = questionService;
        this.gptService = gptService;
    }

    public String ask(TutorRequest request) throws Exception {

        Question question = questionService.findById(request.getQuestionId());

        if (question == null) {
            throw new RuntimeException("Questão não encontrada");
        }

        Answer selectedAnswer = question.getAnswers()
                .stream()
                .filter(a -> a.getId().equals(request.getSelectedAnswerId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Resposta selecionada não encontrada"));

        Answer correctAnswer = question.getAnswers()
                .stream()
                .filter(Answer::isCorrect)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Resposta correta não encontrada"));

        String prompt = buildPrompt(
                question,
                selectedAnswer,
                correctAnswer,
                request.getMessage()
        );

        return gptService.askAssistant(prompt);
    }

    private String buildPrompt(
            Question question,
            Answer selectedAnswer,
            Answer correctAnswer,
            String userMessage) {

        StringBuilder prompt = new StringBuilder();

        // ========================
        // IDENTIDADE DO TUTOR
        // ========================
        prompt.append("Você é um tutor inteligente da plataforma Dikahub.\n");
        prompt.append("Você ensina como um professor particular paciente.\n\n");

        // ========================
        // REGRAS DE COMPORTAMENTO
        // ========================
        prompt.append("REGRAS:\n");
        prompt.append("- Responda diretamente à dúvida do aluno.\n");
        prompt.append("- Seja claro, simples e didático.\n");
        prompt.append("- Não seja excessivamente longo.\n");
        prompt.append("- Explique erros e acertos.\n");
        prompt.append("- Use exemplos quando necessário.\n");
        prompt.append("- Se houver matemática, use LaTeX.\n");
        prompt.append("- Máximo recomendado: 12-15 linhas.\n\n");

        // ========================
        // QUESTÃO
        // ========================
        prompt.append("QUESTÃO:\n");
        prompt.append(question.getText()).append("\n\n");

        if (question.getTopic() != null) {
            prompt.append("TÓPICO: ")
                    .append(question.getTopic().getName())
                    .append("\n\n");
        }

        // ========================
        // ALTERNATIVAS
        // ========================
        prompt.append("ALTERNATIVAS:\n");
        for (Answer answer : question.getAnswers()) {
            prompt.append("- ").append(answer.getText()).append("\n");
        }
        prompt.append("\n");

        // ========================
        // EXPRESSÕES MATEMÁTICAS
        // ========================
        if (question.getMathExpressions() != null &&
                !question.getMathExpressions().isEmpty()) {

            prompt.append("EXPRESSÕES MATEMÁTICAS:\n");
            for (MathExpression exp : question.getMathExpressions()) {

                prompt.append("- ");
                if (exp.getName() != null) {
                    prompt.append(exp.getName()).append(": ");
                }
                prompt.append(exp.getExpression()).append("\n");
            }
            prompt.append("\n");
        }

        // ========================
        // RESPOSTAS
        // ========================
        prompt.append("RESPOSTA DO ALUNO:\n");
        prompt.append(selectedAnswer.getText()).append("\n\n");

        prompt.append("RESPOSTA CORRETA:\n");
        prompt.append(correctAnswer.getText()).append("\n\n");

        // ========================
        // SOLUÇÃO (se existir)
        // ========================
        if (question.getSolution() != null &&
                !question.getSolution().trim().isEmpty()) {

            prompt.append("SOLUÇÃO OFICIAL:\n");
            prompt.append(question.getSolution()).append("\n\n");
        }

        // ========================
        // DICA (se existir)
        // ========================
        if (question.getTip() != null &&
                !question.getTip().trim().isEmpty()) {

            prompt.append("DICA:\n");
            prompt.append(question.getTip()).append("\n\n");
        }

        // ========================
        // PERGUNTA DO ALUNO
        // ========================
        prompt.append("PERGUNTA DO ALUNO:\n");
        prompt.append(userMessage != null ? userMessage : "").append("\n\n");

        // ========================
        // INSTRUÇÕES FINAIS
        // ========================
        prompt.append("ESTRUTURA DA RESPOSTA:\n");
        prompt.append("1. Responda à dúvida do aluno.\n");
        prompt.append("2. Explique o raciocínio passo a passo.\n");
        prompt.append("3. Explique o erro do aluno (se existir).\n");
        prompt.append("4. Mostre como chegar à resposta correta.\n");
        prompt.append("5. Termine com uma dica para fixação.\n");

        return prompt.toString();
    }
}
