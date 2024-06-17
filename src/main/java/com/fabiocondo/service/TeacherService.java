package com.fabiocondo.service;

import com.fabiocondo.domain.Teacher;
import com.fabiocondo.exception.domain.TeacherNotFoundException;
import com.fabiocondo.repository.TeacherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TeacherService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public Teacher findById(Long id) throws TeacherNotFoundException {
        logger.info("Getting course by id: " + id);
        return teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("No teacher found by id: " + id));
    }

    public Page<Teacher> findAll(Pageable pageable) {
        return teacherRepository.findAll(pageable);
    }

    public Page<Teacher> findByName(String name, Pageable pageable) {
        return teacherRepository.findByName(name, pageable);
    }

    public Teacher save(Teacher teacher)  {
        logger.info("Saving teacher: " + teacher.getName());
        return teacherRepository.save(teacher);
    }

    public Teacher update(Teacher teacher, Long id) throws TeacherNotFoundException {
        Teacher existTeacher = findById(id);
        BeanUtils.copyProperties(teacher, existTeacher, "id");
        logger.info("Updating teacher: " + teacher.getName());
        return teacherRepository.save(existTeacher);
    }

    public void delete(Long id) throws TeacherNotFoundException {
        Teacher existTeacher = findById(id);
        logger.info("Deleting teacher: " + existTeacher.getName());
        teacherRepository.deleteById(id);
    }

    public long getTotal(){
        logger.info("Total teacher: " + teacherRepository.count());
        return teacherRepository.count();
    }
}
