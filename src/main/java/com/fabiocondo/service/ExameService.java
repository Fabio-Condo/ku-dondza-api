package com.fabiocondo.service;

import com.fabiocondo.domain.Exame;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.repository.ExameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
public class ExameService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ExameRepository exameRepository;

    @Autowired
    public ExameService(ExameRepository exameRepository) {
        this.exameRepository = exameRepository;
    }

    public Exame findById(Long id) throws ExameNotFoundException {
        return exameRepository.findById(id)
                .orElseThrow(() -> new ExameNotFoundException("No exame found by id: " + id));
    }

    public Page<Exame> findAll(Pageable pageable) {
        return exameRepository.findAll(pageable);
    }

    public List<Exame> findAll() {
        return exameRepository.findAll();
    }

    public Exame save(Exame exame) {
        exame.setTotalDownloadNumber(0L);
        exame.setDate(new Date()); // Deve se fornecida
        exame.setUrlFile("https://www.absa.co.mz/pt/banca-pessoal/borrow/consumer-loans/");
        logger.info("Saving new exame: " + exame.getDescription());
        return exameRepository.save(exame);
    }

    public Exame save(String subject, String description, String level, MultipartFile file)  {
        Exame exame = new Exame();
        exame.setSubject(subject);
        exame.setDescription(description);
        exame.setLevel(level);
        exame.setDate(new Date());
        exame.setTotalDownloadNumber(0L);
        exame.setUrlFile("https://www.absa.co.mz/pt/banca-pessoal/borrow/consumer-loans/");
        logger.info("Saving new exame: " + exame.getDescription());
        return exameRepository.save(exame);
    }

    public Exame update(Long id, String subject, String description, String level, MultipartFile file) throws ExameNotFoundException {
        Exame existExame = findById(id);
        existExame.setSubject(subject);
        existExame.setDescription(description);
        existExame.setLevel(level);
        existExame.setDate(new Date());
        existExame.setTotalDownloadNumber(0L);
        existExame.setUrlFile("https://www.absa.co.mz/pt/banca-pessoal/borrow/consumer-loans/");
        logger.info("Saving new exame: " + existExame.getDescription());
        return exameRepository.save(existExame);
    }

    public Exame update(Exame exame, Long id) throws ExameNotFoundException {
        Exame existExame = findById(id);
        exame.setDate(new Date()); // Deve se fornecida
        exame.setUrlFile("https://www.absa.co.mz/pt/banca-pessoal/borrow/consumer-loans/");
        BeanUtils.copyProperties(exame, existExame, "id","totalDownloadNumber");
        logger.info("Updating exame: " + existExame.getDescription());
        return exameRepository.save(existExame);
    }

    public void delete(Long id) throws ExameNotFoundException {
        Exame existExame = findById(id);
        logger.info("Deleting exame: " + existExame.getDescription());
        exameRepository.deleteById(id);
    }

    public long getTotal(){
        logger.info("Total exames: " + exameRepository.count());
        return exameRepository.count();
    }
}
