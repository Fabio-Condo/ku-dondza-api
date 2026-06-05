package com.fabiocondo.tutor_ai;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tutor")
public class TutorAiController {

    private final TutorAiService tutorAiService;

    public TutorAiController(TutorAiService tutorAiService) {
        this.tutorAiService = tutorAiService;
    }

    @PostMapping("/ask")
    public String ask(@RequestBody TutorRequest request) throws Exception {
        return tutorAiService.ask(request);
    }
}
