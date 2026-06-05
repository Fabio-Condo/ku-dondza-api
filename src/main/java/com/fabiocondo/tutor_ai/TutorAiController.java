package com.fabiocondo.tutor_ai;

import com.fabiocondo.domain.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tutor-ai")
public class TutorAiController {

    private final TutorAiService tutorAiService;

    public TutorAiController(TutorAiService tutorAiService) {
        this.tutorAiService = tutorAiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestBody TutorRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(tutorAiService.ask(request));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
