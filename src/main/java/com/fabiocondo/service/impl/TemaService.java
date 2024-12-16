package com.fabiocondo.service.impl;

import com.fabiocondo.domain.OnlineCourse;
import com.fabiocondo.domain.Tema;
import com.fabiocondo.exception.domain.CourseContentNotFoundException;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.exception.domain.TemaNotFoundException;
import com.fabiocondo.repository.TemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemaService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TemaRepository temaRepository;
    private final OnlineCourseService onlineCourseService;

    public TemaService(TemaRepository temaRepository, OnlineCourseService onlineCourseService) {
        this.temaRepository = temaRepository;
        this.onlineCourseService = onlineCourseService;
    }

    public Tema findById(Long id) throws TemaNotFoundException {
        logger.info("Getting tema by id: " + id);
        return temaRepository.findById(id)
                .orElseThrow(() -> new TemaNotFoundException("No tema found by id: " + id));
    }

    public Page<Tema> findByOnlineCourseId(Long courseId, Pageable pageable) {
        return temaRepository.findByOnlineCourseId(courseId, pageable);
    }

    public List<Tema> findByOnlineCourseId(Long courseId) {
        return temaRepository.findByOnlineCourseId(courseId);
    }

    public Page<Tema> findAll(String searchParam, Pageable pageable) {
        return temaRepository.findAll(searchParam, pageable);
    }

    public List<Tema> findAll() {
        return temaRepository.findAll();
    }

    public Tema save(String name, Long onlineCourseId) throws CourseNotFoundException, TemaNotFoundException {
        OnlineCourse onlineCourse = onlineCourseService.findById(onlineCourseId);
        Tema tema = new Tema();
        tema.setName(name);
        tema.setOnlineCourse(onlineCourse);
        logger.info("Saving new tema: " + tema.getName());
        return temaRepository.save(tema);
    }

    public Tema update(Long id, String name, Long onlineCourseId) throws CourseContentNotFoundException, CourseNotFoundException, TemaNotFoundException {
        OnlineCourse onlineCourse = onlineCourseService.findById(onlineCourseId);
        Tema existTema = findById(id);
        existTema.setOnlineCourse(onlineCourse);
        existTema.setName(name);
        logger.info("Updating tema: " + existTema.getName());
        return temaRepository.save(existTema);
    }

    public void delete(Long id) throws TemaNotFoundException {
        Tema existTema = findById(id);
        temaRepository.deleteById(existTema.getId());
    }

    public long getTotal(){
        return temaRepository.count();
    }
}
