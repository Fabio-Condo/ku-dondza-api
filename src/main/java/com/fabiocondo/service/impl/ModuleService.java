package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Module;
import com.fabiocondo.domain.Course;
import com.fabiocondo.exception.domain.ContentNotFoundException;
import com.fabiocondo.exception.domain.ModuleNotFoundException;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.repository.ModuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModuleService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ModuleRepository moduleRepository;
    private final CourseService courseService;

    public ModuleService(ModuleRepository moduleRepository, CourseService courseService) {
        this.moduleRepository = moduleRepository;
        this.courseService = courseService;
    }

    public Module findById(Long id) throws ModuleNotFoundException {
        logger.info("Getting module by id: " + id);
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundException("No module found by id: " + id));
    }

    public List<Module> findByOnlineCourseId(Long courseId) {
        return moduleRepository.findByCourseIdOrderByPositionAsc(courseId);
    }

    public Page<Module> findAll(String searchParam, Pageable pageable) {
        return moduleRepository.findAll(searchParam, pageable);
    }

    public List<Module> findAll() {
        return moduleRepository.findAll();
    }

    public Module save(String name, Long onlineCourseId, Integer position) throws ModuleNotFoundException, CourseNotFoundException {
        Course course = courseService.findById(onlineCourseId);
        Module module = new Module();
        module.setName(name);
        module.setCourse(course);
        module.setPosition(position);
        logger.info("Saving new module: " + module.getName());
        return moduleRepository.save(module);
    }

    public Module update(Long id, String name, Long onlineCourseId, Integer position) throws ContentNotFoundException, ModuleNotFoundException, CourseNotFoundException {
        Course course = courseService.findById(onlineCourseId);
        Module existModule = findById(id);
        existModule.setCourse(course);
        existModule.setName(name);
        existModule.setPosition(position);
        logger.info("Updating module: " + existModule.getName());
        return moduleRepository.save(existModule);
    }

    public void delete(Long id) throws ModuleNotFoundException {
        Module existModule = findById(id);
        moduleRepository.deleteById(existModule.getId());
    }

    public long getTotal(){
        return moduleRepository.count();
    }
}
