package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Submission;
import com.fabiocondo.dto.RankingDTO;
import com.fabiocondo.repository.filter.SubmissionFilter;
import com.fabiocondo.repository.query.SubmissionRepositoryQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubmissionRepositoryImpl implements SubmissionRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Submission> filter(SubmissionFilter articleFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Submission> criteria = builder.createQuery(Submission.class);
        Root<Submission> root = criteria.from(Submission.class);

        getSortOrder(articleFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(articleFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Submission> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(articleFilter));
    }

    @Override
    public Page<RankingDTO> getRanking(Long competitionId, Pageable pageable) {
        String baseQuery = "SELECT new com.fabiocondo.dto.RankingDTO(" +
                "s.user.fullName, s.user.id, " +
                "SUM(CASE WHEN a.isCorrect = true THEN 1 ELSE 0 END), " +
                "COUNT(a), " +
                "MIN(s.submittedAt), " +
                "s.user.profileImageUrl, " +
                "s.user.userId) " +
                "FROM Submission s " +
                "LEFT JOIN s.answers a " +
                "WHERE s.competition.id = :competitionId " +
                "GROUP BY s.user.id, s.user.fullName, s.user.profileImageUrl, s.user.userId " +
                "ORDER BY SUM(CASE WHEN a.isCorrect = true THEN 1 ELSE 0 END) DESC, MIN(s.submittedAt) ASC";

        TypedQuery<RankingDTO> query = manager.createQuery(baseQuery, RankingDTO.class);
        query.setParameter("competitionId", competitionId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<RankingDTO> content = query.getResultList();

        String countQuery = "SELECT COUNT(DISTINCT s.user.id) FROM Submission s WHERE s.competition.id = :competitionId";
        Long total = manager.createQuery(countQuery, Long.class)
                .setParameter("competitionId", competitionId)
                .getSingleResult();

        int startPosition = (int) pageable.getOffset() + 1;
        for (int i = 0; i < content.size(); i++) {
            content.get(i).setPosition(startPosition + i);
        }

        return new PageImpl<>(content, pageable, total);
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(SubmissionFilter submissionFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Submission> root = criteria.from(Submission.class);

        Predicate[] predicates = createRestrictions(submissionFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(SubmissionFilter submissionFilter, CriteriaBuilder builder, Root<Submission> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(submissionFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(SubmissionFilter submissionFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Submission> root){

        if(!ObjectUtils.isEmpty(submissionFilter.getSearchParam())) {
            Predicate title = builder.like(
                    builder.lower(root.get("title")), "%" + submissionFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(title));
        }
    }

    public void getSortOrder(SubmissionFilter submissionFilter, CriteriaBuilder builder, CriteriaQuery<Submission> criteria, Root<Submission> root){
        if(Objects.equals(submissionFilter.getSort(), "id,asc")){
            criteria.orderBy(builder.asc(root.get("id")));
        }
        if(Objects.equals(submissionFilter.getSort(), "id,desc")){
            criteria.orderBy(builder.desc(root.get("id")));
        }
    }
}
