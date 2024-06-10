package com.fabiocondo.repository;

import com.fabiocondo.domain.Exame;
import com.fabiocondo.repository.query.ExameRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExameRepository extends JpaRepository<Exame, Long>, ExameRepositoryQuery {
}
