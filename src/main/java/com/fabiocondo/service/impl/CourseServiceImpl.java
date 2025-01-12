package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Course;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.repository.CourseRepository;
import com.fabiocondo.repository.filter.CourseFilter;
import com.fabiocondo.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Course findById(Long id) throws CourseNotFoundException {
        logger.info("Getting course by id: " + id);
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("No course found by id: " + id));
    }

    @Override
    public Page<Course> findAll(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Override
    public Page<Course> filter(CourseFilter courseFilter, Pageable pageable) {
        return courseRepository.filter(courseFilter, pageable);
    }

    @Override
    public Page<Course> findByName(String name, Pageable pageable) {
        return courseRepository.findByName(name, pageable);
    }

    @Override
    public Page<Course> findByInstitutionId(@RequestParam Long institutionId, Pageable pageable) {
        return courseRepository.findByInstitutionId(institutionId, pageable);
    }

    @Override
    public Course save(Course course) {
        course.getRequirements().forEach(requirement -> requirement.setCourse(course));
        logger.info("Saving course: " + course.getName());
        return courseRepository.save(course);
    }

    @Override
    public Course update(Course course, Long id) throws CourseNotFoundException {
        Course existCourse = findById(id);
        existCourse.getRequirements().clear();
        existCourse.getRequirements().addAll(course.getRequirements());
        existCourse.getRequirements().forEach(requirement -> requirement.setCourse(existCourse));
        BeanUtils.copyProperties(course, existCourse, "id", "requirements");
        logger.info("Updating course: " + course.getName());
        return courseRepository.save(existCourse);
    }

    @Override
    public void delete(Long id) throws CourseNotFoundException {
        Course existCourse = findById(id);
        logger.info("Deleting course: " + existCourse.getName());
        courseRepository.deleteById(id);
    }

    @Override
    public long getTotal(){
        logger.info("Total course: " + courseRepository.count());
        return courseRepository.count();
    }

    @Override
    public List<Course> getByInstitutionId(Long institutionId) {
        return courseRepository.findByInstitutionIdOrderByNameAsc(institutionId);
    }

}
