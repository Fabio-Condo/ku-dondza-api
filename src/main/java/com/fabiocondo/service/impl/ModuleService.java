package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Module;
import com.fabiocondo.domain.OnlineCourse;
import com.fabiocondo.exception.domain.CourseContentNotFoundException;
import com.fabiocondo.exception.domain.ModuleNotFoundException;
import com.fabiocondo.exception.domain.OnlineCourseNotFoundException;
import com.fabiocondo.repository.TemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModuleService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TemaRepository temaRepository;
    private final OnlineCourseService onlineCourseService;

    public ModuleService(TemaRepository temaRepository, OnlineCourseService onlineCourseService) {
        this.temaRepository = temaRepository;
        this.onlineCourseService = onlineCourseService;
    }

    public Module findById(Long id) throws ModuleNotFoundException {
        logger.info("Getting module by id: " + id);
        return temaRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundException("No module found by id: " + id));
    }

    public List<Module> findByOnlineCourseId(Long courseId) {
        return temaRepository.findByOnlineCourseIdOrderByPositionAsc(courseId);
    }

    public Page<Module> findAll(String searchParam, Pageable pageable) {
        return temaRepository.findAll(searchParam, pageable);
    }

    public List<Module> findAll() {
        return temaRepository.findAll();
    }

    public Module save(String name, Long onlineCourseId, Integer position) throws ModuleNotFoundException, OnlineCourseNotFoundException {
        OnlineCourse onlineCourse = onlineCourseService.findById(onlineCourseId);
        Module module = new Module();
        module.setName(name);
        module.setOnlineCourse(onlineCourse);
        module.setPosition(position);
        logger.info("Saving new module: " + module.getName());
        return temaRepository.save(module);
    }

    public Module update(Long id, String name, Long onlineCourseId, Integer position) throws CourseContentNotFoundException, ModuleNotFoundException, OnlineCourseNotFoundException {
        OnlineCourse onlineCourse = onlineCourseService.findById(onlineCourseId);
        Module existModule = findById(id);
        existModule.setOnlineCourse(onlineCourse);
        existModule.setName(name);
        existModule.setPosition(position);
        logger.info("Updating module: " + existModule.getName());
        return temaRepository.save(existModule);
    }

    public void delete(Long id) throws ModuleNotFoundException {
        Module existModule = findById(id);
        temaRepository.deleteById(existModule.getId());
    }

    public long getTotal(){
        return temaRepository.count();
    }
}
