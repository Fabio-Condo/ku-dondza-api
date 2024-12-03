package com.fabiocondo.service.impl;

import com.fabiocondo.domain.OnlineCourseContent;
import com.fabiocondo.domain.Tema;
import com.fabiocondo.exception.domain.TemaNotFoundException;
import com.fabiocondo.repository.TemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemaService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TemaRepository temaRepository;

    public TemaService(TemaRepository temaRepository) {
        this.temaRepository = temaRepository;
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

    public Tema save(Tema tema) {
        return temaRepository.save(tema);
    }

    public Tema update(Tema tema, Long id) throws TemaNotFoundException {
        Tema existTema = findById(id);
        BeanUtils.copyProperties(tema, existTema, "id");
        return temaRepository.save(existTema);
    }

    public void delete(Long id) throws TemaNotFoundException {
        Tema existTema = findById(id);
        temaRepository.deleteById(id);
    }

    public long getTotal(){
        return temaRepository.count();
    }
}
