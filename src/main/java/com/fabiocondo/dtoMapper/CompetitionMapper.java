package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Article;
import com.fabiocondo.domain.Competition;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.ArticleDTO;
import com.fabiocondo.dto.CompetitionDto;
import com.fabiocondo.exception.domain.ArticleNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.impl.CompetitionService;
import com.fabiocondo.service.impl.SubmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.fabiocondo.constant.UserImplConstant.NO_USER_FOUND_BY_EMAIL;

@Component
public class CompetitionMapper {

    private final CompetitionService competitionService;
    private final SubmissionService submissionService;
    private final UserRepository userRepository;


    public CompetitionMapper(CompetitionService competitionService, SubmissionService submissionService, UserRepository userRepository) {
        this.competitionService = competitionService;
        this.submissionService = submissionService;
        this.userRepository = userRepository;
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

    public CompetitionDto domainToDTO(Competition competition, Long currentUserId) {
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
        competitionDto.setCurrentUserHasSubmitted(submissionService.hasUserAlreadySubmitted(competition.getId() ,currentUserId));
        competitionDto.setTopics(competitionService.getTopicsByCompetitionId(competition.getId()));
        competitionDto.setTotalTopics((long) competitionDto.getTopics().size());
        competitionDto.setTotalQuestions(competitionService.countQuestionsByCompetitionId(competition.getId()));
        competitionDto.setTotalSubmissions(submissionService.countByCompetitionId(competition.getId()));
        return competitionDto;
    }

    public Page<CompetitionDto> domainPageToDTOPage(Page<Competition> competitions, Long currentUserId, Pageable pageable) {

        return new PageImpl<>(competitions.stream()
                .map(competition -> {
                    return domainToDTO(competition, currentUserId);
                })
                .collect(Collectors.toList()), pageable, competitions.getTotalElements());

    }
}
