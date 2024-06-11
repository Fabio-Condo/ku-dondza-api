package com.fabiocondo.service;

import com.fabiocondo.domain.Instituicao;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.repository.InstituicaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InstituicaoService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    InstituicaoRepository instituicaoRepository;

    public InstituicaoService(InstituicaoRepository instituicaoRepository) {
        this.instituicaoRepository = instituicaoRepository;
    }

    public Instituicao findById(Long id) throws InstituicaoNotFoundException {
        return instituicaoRepository.findById(id)
                .orElseThrow(() -> new InstituicaoNotFoundException("No instituicao found by id: " + id));
    }

    public Instituicao save(Instituicao instituicao)  {
        return instituicaoRepository.save(instituicao);
    }

    public Instituicao update(Instituicao instituicao, Long id) throws InstituicaoNotFoundException {
        Instituicao instituicaoE = findById(id);
        return instituicaoRepository.save(instituicaoE);
    }

    public void delete(Long id) throws InstituicaoNotFoundException {
        Instituicao instituicao = findById(id);
        logger.info("Deleting instituicao: " + instituicao.getLevel());
        instituicaoRepository.deleteById(id);
    }

    public long getTotal(){
        logger.info("Total instituicao: " + instituicaoRepository.count());
        return instituicaoRepository.count();
    }
}
