package com.fabiocondo.tutor_ai;

import com.fabiocondo.domain.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tutor-ai")
public class TutorAiController {

    private final TutorQuestionService tutorQuestionService;
    private final TutorTopicService tutorTopicService;


    public TutorAiController(TutorQuestionService tutorQuestionService, TutorTopicService tutorTopicService) {
        this.tutorQuestionService = tutorQuestionService;
        this.tutorTopicService = tutorTopicService;
    }

    @PostMapping("/ask/question")
    public ResponseEntity<String> askQuestions(@RequestBody TutorRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(tutorQuestionService.ask(request));
    }

    @PostMapping("/ask/topic")
    public ResponseEntity<String> askTopics(@RequestBody TutorRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(tutorTopicService.ask(request));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
