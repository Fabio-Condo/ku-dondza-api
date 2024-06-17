package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Institution;
import com.fabiocondo.repository.filter.InstitutionFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InstitutionRepositoryQuery {
    public Page<Institution> filter(InstitutionFilter institutionFilter, Pageable pageable);
}
