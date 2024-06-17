package com.fabiocondo.service;

import com.fabiocondo.domain.Course;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course findById(Long id) throws CourseNotFoundException {
        logger.info("Getting course by id: " + id);
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("No course found by id: " + id));
    }

    public Page<Course> findAll(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    public Page<Course> findByName(String name, Pageable pageable) {
        return courseRepository.findByName(name, pageable);
    }

    public Course save(Course course)  {
        logger.info("Saving course: " + course.getName());
        return courseRepository.save(course);
    }

    public Course update(Course course, Long id) throws CourseNotFoundException {
        Course existCourse = findById(id);
        BeanUtils.copyProperties(course, existCourse, "id");
        logger.info("Updating course: " + course.getName());
        return courseRepository.save(existCourse);
    }

    public void delete(Long id) throws CourseNotFoundException {
        Course existCourse = findById(id);
        logger.info("Deleting course: " + existCourse.getName());
        courseRepository.deleteById(id);
    }

    public long getTotal(){
        logger.info("Total course: " + courseRepository.count());
        return courseRepository.count();
    }

}
