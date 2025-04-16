package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.OnlineCourseDTO;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.OnlineCourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OnlineCourseMapper {

    private final OnlineCourseService onlineCourseService;

    public OnlineCourseMapper(OnlineCourseService onlineCourseService) {
        this.onlineCourseService = onlineCourseService;
    }

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
        return onlineCourse;
    }

    public OnlineCourseDTO domainToDTO_WithModules(OnlineCourse onlineCourse) throws UserNotFoundException {
        OnlineCourseDTO onlineCourseDTO = new OnlineCourseDTO();
        onlineCourseDTO.setId(onlineCourse.getId());
        onlineCourseDTO.setOnlineCourseId(onlineCourse.getOnlineCourseId());
        onlineCourseDTO.setName(onlineCourse.getName());
        onlineCourseDTO.setDescription(onlineCourse.getDescription());
        onlineCourseDTO.setFileName(onlineCourse.getFileName());
        onlineCourseDTO.setCoverImageUrl(onlineCourse.getCoverImageUrl());
        onlineCourseDTO.setLunchDate(onlineCourse.getLunchDate());
        onlineCourseDTO.setInstrutor(onlineCourse.getInstrutor());
        onlineCourseDTO.setModules(onlineCourse.getModules());
        onlineCourseDTO.setCurrentUserSubscribed(onlineCourseService.checkIfCurrentUserSubscribed(onlineCourse.getId()));
        return onlineCourseDTO;
    }

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
        return onlineCourseDTO;
    }

    public Page<OnlineCourseDTO> domainPageToDTOPage(Page<OnlineCourse> onlineCourses, Pageable pageable) {
        return new PageImpl<>(onlineCourses.stream()
                .map(this::domainToDTO)
                .collect(Collectors.toList()), pageable, onlineCourses.getTotalElements());
    }
}

