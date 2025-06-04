package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Submission;
import com.fabiocondo.dto.RankingDTO;
import com.fabiocondo.repository.filter.SubmissionFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubmissionRepositoryQuery {
    public Page<Submission> filter(SubmissionFilter submissionFilter, Pageable pageable);

    Page<RankingDTO> getRanking(Long competitionId, Pageable pageable);
}
