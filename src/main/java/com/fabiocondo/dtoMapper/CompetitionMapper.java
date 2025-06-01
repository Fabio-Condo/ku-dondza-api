package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Competition;
import com.fabiocondo.dto.CompetitionDto;
import com.fabiocondo.service.impl.CompetitionService;
import com.fabiocondo.service.impl.SubmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CompetitionMapper {

    private final CompetitionService competitionService;
    private final SubmissionService submissionService;


    public CompetitionMapper(CompetitionService competitionService, SubmissionService submissionService) {
        this.competitionService = competitionService;
        this.submissionService = submissionService;
    }

    public Competition dtoToDomainObject(CompetitionDto competitionDto) {
        Competition competition = new Competition();
        competition.setCompetitionId(competitionDto.getCompetitionId());
        competition.setTitle(competitionDto.getTitle());
        competition.setDifficultyLevel(competitionDto.getDifficultyLevel());
        competition.setSubject(competitionDto.getSubject());
        return competition;
    }

    public CompetitionDto domainToDTO_WithQuestions(Competition competition) {
        CompetitionDto competitionDto = new CompetitionDto();
        competitionDto.setId(competition.getId());
        competitionDto.setCompetitionId(competition.getCompetitionId());
        competitionDto.setActive(competition.isOpen());
        competitionDto.setExpiry(competition.getExpiry());
        competitionDto.setTitle(competition.getTitle());
        competitionDto.setDifficultyLevel(competition.getDifficultyLevel());
        competitionDto.setLimitPerTopic(competition.getLimitPerTopic());
        competitionDto.setTimeLimit(competition.getTimeLimit());
        competitionDto.setTimeSpent(competition.getTimeSpent());
        competitionDto.setSubject(competition.getSubject());
        competitionDto.setQuestions(competition.getQuestions());
        competitionDto.setTopics(competitionService.getTopicsByCompetitionId(competition.getId()));
        competitionDto.setTotalTopics((long) competitionDto.getTopics().size());
        competitionDto.setTotalQuestions(competitionService.countQuestionsByCompetitionId(competition.getId()));
        competitionDto.setTotalSubmissions(submissionService.countByCompetitionId(competition.getId()));
        return competitionDto;
    }

    public CompetitionDto domainToDTO(Competition competition) {
        CompetitionDto competitionDto = new CompetitionDto();
        competitionDto.setId(competition.getId());
        competitionDto.setCompetitionId(competition.getCompetitionId());
        competitionDto.setActive(competition.isOpen());
        competitionDto.setExpiry(competition.getExpiry());
        competitionDto.setTitle(competition.getTitle());
        competitionDto.setDifficultyLevel(competition.getDifficultyLevel());
        competitionDto.setLimitPerTopic(competition.getLimitPerTopic());
        competitionDto.setTimeLimit(competition.getTimeLimit());
        competitionDto.setTimeSpent(competition.getTimeSpent());
        competitionDto.setSubject(competition.getSubject());
        competitionDto.setTopics(competitionService.getTopicsByCompetitionId(competition.getId()));
        competitionDto.setTotalTopics((long) competitionDto.getTopics().size());
        competitionDto.setTotalQuestions(competitionService.countQuestionsByCompetitionId(competition.getId()));
        competitionDto.setTotalSubmissions(submissionService.countByCompetitionId(competition.getId()));
        return competitionDto;
    }

    public Page<CompetitionDto> domainPageToDTOPage(Page<Competition> competitions, Pageable pageable) {
        return new PageImpl<>(competitions.stream()
                .map(this::domainToDTO)
                .collect(Collectors.toList()), pageable, competitions.getTotalElements());
    }
}
