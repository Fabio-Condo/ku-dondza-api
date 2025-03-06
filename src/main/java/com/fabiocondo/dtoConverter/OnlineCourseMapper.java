package com.fabiocondo.dtoConverter;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.OnlineCourseDTO;
import com.fabiocondo.repository.OnlineCourseRepository;
import com.fabiocondo.service.impl.ModuleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OnlineCourseMapper {

    private final OnlineCourseRepository onlineCourseRepository;
    private final ModuleService moduleService;

    public OnlineCourseMapper(OnlineCourseRepository onlineCourseRepository, ModuleService moduleService) {
        this.onlineCourseRepository = onlineCourseRepository;
        this.moduleService = moduleService;
    }

    // Converter DTO para Entidade (OnlineCourse)
    public OnlineCourse dtoToDomainObject(OnlineCourseDTO onlineCourseDTO) {
        OnlineCourse onlineCourse = new OnlineCourse();
        onlineCourse.setId(onlineCourseDTO.getId());
        onlineCourse.setOnlineCourseId(onlineCourseDTO.getOnlineCourseId());
        onlineCourse.setName(onlineCourseDTO.getName());
        onlineCourse.setDescription(onlineCourseDTO.getDescription());
        onlineCourse.setFileName(onlineCourseDTO.getFileName());
        onlineCourse.setCoverImageUrl(onlineCourseDTO.getCoverImageUrl());
        onlineCourse.setLunchDate(onlineCourseDTO.getLunchDate());
        onlineCourse.setInstrutor(onlineCourseDTO.getInstrutor());
        onlineCourse.setRequirements(onlineCourseDTO.getRequirements());
        return onlineCourse;
    }

    public OnlineCourseDTO domainToDTO_WithModules(OnlineCourse onlineCourse) {
        OnlineCourseDTO onlineCourseDTO = new OnlineCourseDTO();
        onlineCourseDTO.setId(onlineCourse.getId());
        onlineCourseDTO.setOnlineCourseId(onlineCourse.getOnlineCourseId());
        onlineCourseDTO.setName(onlineCourse.getName());
        onlineCourseDTO.setDescription(onlineCourse.getDescription());
        onlineCourseDTO.setFileName(onlineCourse.getFileName());
        onlineCourseDTO.setCoverImageUrl(onlineCourse.getCoverImageUrl());
        onlineCourseDTO.setLunchDate(onlineCourse.getLunchDate());
        onlineCourseDTO.setInstrutor(onlineCourse.getInstrutor());
        onlineCourseDTO.setRequirements(onlineCourse.getRequirements());
        onlineCourseDTO.setModules(onlineCourse.getModules());
        return onlineCourseDTO;
    }

    // Converter Entidade (OnlineCourse) para DTO
    public OnlineCourseDTO domainToDTO(OnlineCourse onlineCourse) {
        OnlineCourseDTO onlineCourseDTO = new OnlineCourseDTO();
        onlineCourseDTO.setId(onlineCourse.getId());
        onlineCourseDTO.setOnlineCourseId(onlineCourse.getOnlineCourseId());
        onlineCourseDTO.setName(onlineCourse.getName());
        onlineCourseDTO.setDescription(onlineCourse.getDescription());
        onlineCourseDTO.setFileName(onlineCourse.getFileName());
        onlineCourseDTO.setCoverImageUrl(onlineCourse.getCoverImageUrl());
        onlineCourseDTO.setLunchDate(onlineCourse.getLunchDate());
        onlineCourseDTO.setInstrutor(onlineCourse.getInstrutor());
        onlineCourseDTO.setRequirements(onlineCourse.getRequirements());
        return onlineCourseDTO;
    }



    // Converter lista paginada de Quiz para DTO
    public Page<OnlineCourseDTO> domainPageToDTOPage(Page<OnlineCourse> onlineCourses, Pageable pageable) {
        return new PageImpl<>(onlineCourses.stream()
                .map(this::domainToDTO)
                .collect(Collectors.toList()), pageable, onlineCourses.getTotalElements());
    }
}

