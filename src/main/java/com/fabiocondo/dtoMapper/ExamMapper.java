package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Exam;
import com.fabiocondo.dto.ExamDto;
import com.fabiocondo.dto.SubjectDto;
import org.springframework.stereotype.Component;

@Component
public class ExamMapper {

    public Exam dtoToDomainObject(ExamDto examDto) {
        Exam exam = new Exam();
        exam.setId(examDto.getId());
        exam.setExamType(examDto.getExamType());
        exam.setInstitution(examDto.getInstitution());
        exam.setPremium(examDto.isPremium());
        exam.setFileName(examDto.getFileName());
        exam.setUrlFile(examDto.getUrlFile());
        exam.setYear(examDto.getYear());
        exam.setTotalDownloadNumber(examDto.getTotalDownloadNumber());
        exam.setNumber(examDto.getNumber());

        // CONVERTER Subject para SubjectDTO
        if (exam.getSubject() != null) {
            SubjectDto subjectDTO = new SubjectDto();
            subjectDTO.setId(exam.getSubject().getId());
            subjectDTO.setName(exam.getSubject().getName());
            subjectDTO.setDescription(exam.getSubject().getDescription());
            subjectDTO.setCategory(exam.getSubject().getCategory());
            examDto.setSubject(subjectDTO);
        }

        return exam;
    }

    public ExamDto domainToDTO(Exam exam) {
        ExamDto examDto = new ExamDto();
        examDto.setId(exam.getId());
        examDto.setExamType(exam.getExamType());
        examDto.setInstitution(exam.getInstitution());
        examDto.setPremium(exam.isPremium());
        examDto.setFileName(exam.getFileName());
        examDto.setUrlFile(exam.getUrlFile());
        examDto.setYear(exam.getYear());
        examDto.setTotalDownloadNumber(exam.getTotalDownloadNumber());
        examDto.setNumber(exam.getNumber());

        // CONVERTER Subject para SubjectDTO
        if (exam.getSubject() != null) {
            SubjectDto subjectDTO = new SubjectDto();
            subjectDTO.setId(exam.getSubject().getId());
            subjectDTO.setName(exam.getSubject().getName());
            subjectDTO.setDescription(exam.getSubject().getDescription());
            subjectDTO.setCategory(exam.getSubject().getCategory());
            examDto.setSubject(subjectDTO);
        }

        return examDto;
    }
}
