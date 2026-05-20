package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Subject;
import com.fabiocondo.dto.PageResponse;
import com.fabiocondo.dto.SubjectDto;
import com.fabiocondo.dto.SubjectProgressDTO;
import com.fabiocondo.dtoMapper.SubjectMapper;
import com.fabiocondo.enumeration.Category;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.SubjectServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/subjects")
public class SubjectController {

    public SubjectServiceImpl subjectServiceImpl;
    public SubjectMapper subjectMapper;

    public SubjectController(SubjectServiceImpl subjectServiceImpl, SubjectMapper subjectMapper) {
        this.subjectServiceImpl = subjectServiceImpl;
        this.subjectMapper = subjectMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> findById(@PathVariable("id") Long id) throws SubjectNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(subjectServiceImpl.findById(id));
    }

    @GetMapping("/find-by-subjectId/{subjectId}")
    public ResponseEntity<SubjectDto> findSubjectBySubjectIdWithCash(
            @PathVariable String subjectId,
            @RequestParam Long currentUserId) throws SubjectNotFoundException, UserNotFoundException {

        return ResponseEntity.ok(
                subjectServiceImpl.findSubjectBySubjectIdWithCash(subjectId, currentUserId)
        );
    }

    // GET com cache
    @GetMapping("/filter")
    public PageResponse<SubjectDto> filterWithCash(
            @RequestParam(required = false) Long subjectId,
            @RequestParam("currentUserId") Long currentUserId,
            @RequestParam("enabled") boolean enabled,
            Pageable pageable
    ) {
        return subjectServiceImpl.findByNameWithCache(
                subjectId,
                enabled,
                currentUserId,
                pageable
        );
    }

    @GetMapping("/progress/users")
    public List<SubjectProgressDTO> getUserProgressSubjects(@RequestParam("currentUserId") Long currentUserId) {
        return subjectMapper.mapSubjectsToProgressDTOs(subjectServiceImpl.findAll(), currentUserId);
    }

    @GetMapping("/progress/users/view")
    public SubjectProgressDTO getUserProgressSubject(@RequestParam("currentUserId") Long currentUserId, @RequestParam("subjectId") String subjectId) throws SubjectNotFoundException {
        return subjectMapper.mapSubjectToProgressDTO(subjectServiceImpl.findSubjectBySubjectId(subjectId), currentUserId);
    }

    @GetMapping
    public ResponseEntity<List<Subject>> findAll() throws SubjectNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(subjectServiceImpl.findAll());
    }

    @PostMapping
    public ResponseEntity<Subject> save(@RequestParam("name") String name,
                                         @RequestParam("description") String description,
                                         @RequestParam("category") Category category,
                                         @RequestParam("quizEnabled") boolean quizEnabled,
                                         @RequestParam("courseEnabled") boolean courseEnabled,
                                         @RequestParam("progressEnabled") boolean progressEnabled,
                                         @RequestParam("examEnabled") boolean examEnabled,
                                         @RequestParam("file") MultipartFile file) {

        return ResponseEntity.status(HttpStatus.OK).body(subjectServiceImpl.save(name, description, category, quizEnabled, courseEnabled, progressEnabled, examEnabled, file));
    }

    @PutMapping
    public ResponseEntity<Subject> update(@RequestParam("id") Long id,
                                           @RequestParam("name") String name,
                                           @RequestParam("description") String description,
                                           @RequestParam("category") Category category,

                                           @RequestParam("quizEnabled") boolean quizEnabled,
                                           @RequestParam("courseEnabled") boolean courseEnabled,
                                           @RequestParam("progressEnabled") boolean progressEnabled,
                                           @RequestParam("examEnabled") boolean examEnabled,

                                           @RequestParam(value = "file", required = false) MultipartFile file) throws SubjectNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(subjectServiceImpl.update(id, name, description, category, quizEnabled, courseEnabled, progressEnabled, examEnabled,file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws SubjectNotFoundException {
        subjectServiceImpl.delete(id);
        return response(HttpStatus.OK, "Subject deleted successfully");
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}