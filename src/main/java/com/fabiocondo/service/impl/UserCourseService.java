package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Course;
import com.fabiocondo.domain.User;
import com.fabiocondo.domain.UserCourse;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.exception.domain.UserCourseNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.CourseRepository;
import com.fabiocondo.repository.UserCourseRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserCourseService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;

    public UserCourseService(UserRepository userRepository, CourseRepository courseRepository, UserCourseRepository userCourseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.userCourseRepository = userCourseRepository;
    }

    public UserCourse addCourseToUser(UserCourse userCourse) {
        return userCourseRepository.save(userCourse);
    }

    public UserCourse updateUserCourse(Long userCourseId, UserCourse userCourse) throws UserCourseNotFoundException {
        UserCourse existeUserCourse = userCourseRepository.findById(userCourseId)
                .orElseThrow(() -> new UserCourseNotFoundException("UserCourse not found by id " + userCourseId));
        BeanUtils.copyProperties(userCourse, existeUserCourse, "id");
        return userCourseRepository.save(existeUserCourse);
    }

    public UserCourse updateUserCourseByUserAndCourse(Long userId, Long courseId, Date startDate) throws UserCourseNotFoundException {
        UserCourse userCourse = userCourseRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new UserCourseNotFoundException("UserCourse not found"));

        if (startDate != null) {
            userCourse.setStartDate(startDate);
        }

        return userCourseRepository.save(userCourse);
    }

    public void removeUserCourse(Long userCourseId) throws UserCourseNotFoundException {
        UserCourse userCourse = userCourseRepository.findById(userCourseId)
                .orElseThrow(() -> new UserCourseNotFoundException("UserCourse not found by id " + userCourseId));

        userCourseRepository.delete(userCourse);
    }

    public Page<UserCourse> getCoursesWithDatesByUser(Long userId, Pageable pageable) {
        return userCourseRepository.findByUserId(userId, pageable);
    }

    public Page<UserCourse> getUsersByCourse(Long courseId, Pageable pageable) {
        return userCourseRepository.findByCourseId(courseId, pageable);
    }

    public boolean isUserEnrolledInCourse(Long userId, Long courseId) throws UserNotFoundException, CourseNotFoundException {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found bay id " + userId);
        }
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException("Course not found by id " + courseId);
        }
        return userCourseRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    /*

    // Spring nao consegui o id como chave primaria, entao deve criar manualmente essa table, remova a criada por spring e crie esta, ou simplesmente adicione o AUTO_INCREMENT PRIMARY KEY no id

    CREATE TABLE user_course (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            user_id BIGINT NOT NULL,
            course_id BIGINT NOT NULL,
            start_date DATE,
            CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT fk_course FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
    );
    */
}

