package com.fabiocondo.repository;

import com.fabiocondo.domain.Exame;
import com.fabiocondo.domain.Instituicao;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {
}
