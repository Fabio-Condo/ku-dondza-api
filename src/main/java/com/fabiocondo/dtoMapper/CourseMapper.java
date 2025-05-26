package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.CourseDTO;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CourseMapper {

    private final CourseService courseService;

    public CourseMapper(CourseService courseService) {
        this.courseService = courseService;
    }

    public Course dtoToDomainObject(CourseDTO courseDTO) {
        Course course = new Course();
        course.setId(courseDTO.getId());
        course.setOnlineCourseId(courseDTO.getOnlineCourseId());
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        course.setFileName(courseDTO.getFileName());
        course.setCoverImageUrl(courseDTO.getCoverImageUrl());
        course.setLunchDate(courseDTO.getLunchDate());
        course.setInstrutor(courseDTO.getInstrutor());
        return course;
    }

    public CourseDTO domainToDTO_WithModules(Course course) throws UserNotFoundException {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setId(course.getId());
        courseDTO.setOnlineCourseId(course.getOnlineCourseId());
        courseDTO.setName(course.getName());
        courseDTO.setDescription(course.getDescription());
        courseDTO.setFileName(course.getFileName());
        courseDTO.setCoverImageUrl(course.getCoverImageUrl());
        courseDTO.setLunchDate(course.getLunchDate());
        courseDTO.setInstrutor(course.getInstrutor());
        courseDTO.setModules(course.getModules()); // Em cada modulo percorrer os contents e verificar se user atual autenticado, marcou o content
        courseDTO.setCurrentUserSubscribed(courseService.checkIfCurrentUserSubscribed(course.getId()));
        courseDTO.setTotalStudents(courseService.getTotalStudentsByCourseId(course.getId()));
        return courseDTO;
    }

    public CourseDTO domainToDTO(Course course) {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setId(course.getId());
        courseDTO.setOnlineCourseId(course.getOnlineCourseId());
        courseDTO.setName(course.getName());
        courseDTO.setDescription(course.getDescription());
        courseDTO.setFileName(course.getFileName());
        courseDTO.setCoverImageUrl(course.getCoverImageUrl());
        courseDTO.setLunchDate(course.getLunchDate());
        courseDTO.setInstrutor(course.getInstrutor());
        return courseDTO;
    }

    public Page<CourseDTO> domainPageToDTOPage(Page<Course> onlineCourses, Pageable pageable) {
        return new PageImpl<>(onlineCourses.stream()
                .map(this::domainToDTO)
                .collect(Collectors.toList()), pageable, onlineCourses.getTotalElements());
    }
}

