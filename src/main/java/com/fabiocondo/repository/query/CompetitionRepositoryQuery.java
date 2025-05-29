package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Competition;
import com.fabiocondo.repository.filter.CompetitionFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompetitionRepositoryQuery {
    public Page<Competition> filter(CompetitionFilter competitionFilter, Pageable pageable);
}
