package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.OnlineCourseDTO;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.OnlineCourseRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.impl.ModuleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.fabiocondo.constant.UserImplConstant.NO_USER_FOUND_BY_USERNAME;

@Component
public class OnlineCourseMapper {

    private final OnlineCourseRepository onlineCourseRepository;
    private final UserRepository userRepository;
    private final ModuleService moduleService;

    public OnlineCourseMapper(OnlineCourseRepository onlineCourseRepository, UserRepository userRepository, ModuleService moduleService) {
        this.onlineCourseRepository = onlineCourseRepository;
        this.userRepository = userRepository;
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
        onlineCourseDTO.setCurrentUserSubscribed(checkIfCurrentUserSubscribed(onlineCourse.getId()));
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
        return onlineCourseDTO;
    }

    public boolean checkIfCurrentUserSubscribed(Long onlineCourseId) throws UserNotFoundException {
        User user = getAuthenticatedUser();
        if (user == null) {
            return false;
        }
        Optional<OnlineCourse> course = onlineCourseRepository.findById(onlineCourseId);
        if (!course.isPresent()) {
            return false;
        }
        return user.getSubscribedOnlineCourses().contains(course.get());
    }

    public User getAuthenticatedUser() throws UserNotFoundException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findUserByEmail(username);
        if(user == null){
            throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
        }
        return userRepository.findUserByEmail(username);
    }

    // Converter lista paginada de Quiz para DTO
    public Page<OnlineCourseDTO> domainPageToDTOPage(Page<OnlineCourse> onlineCourses, Pageable pageable) {
        return new PageImpl<>(onlineCourses.stream()
                .map(this::domainToDTO)
                .collect(Collectors.toList()), pageable, onlineCourses.getTotalElements());
    }
}

