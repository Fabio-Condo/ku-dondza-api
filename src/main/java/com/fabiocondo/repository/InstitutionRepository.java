package com.fabiocondo.repository;

import com.fabiocondo.domain.Group;
import com.fabiocondo.domain.Institution;
import com.fabiocondo.repository.query.InstitutionRepositoryQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface InstitutionRepository extends JpaRepository<Institution, Long>, InstitutionRepositoryQuery {
    @Query("SELECT i FROM Institution i WHERE i.name LIKE %:query%")
    Page<Institution> searchByQuery(@Param("query") String query, Pageable pageable);
}
