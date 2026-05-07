package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.repository.filter.ChallengeFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChallengeRepositoryQuery {
    public Page<Challenge> filter(ChallengeFilter challengeFilter, Pageable pageable);
}
