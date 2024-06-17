package com.fabiocondo.repository;

import com.fabiocondo.domain.Institution;
import com.fabiocondo.repository.query.InstitutionRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InstitutionRepository extends JpaRepository<Institution, Long>, InstitutionRepositoryQuery {
}
