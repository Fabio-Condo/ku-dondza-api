package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Exame;
import com.fabiocondo.repository.filter.ExameFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExameRepositoryQuery {
    public Page<Exame> filter(ExameFilter exameFilter, Pageable pageable);
}
