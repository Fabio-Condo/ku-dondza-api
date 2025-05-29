package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Competition;
import com.fabiocondo.repository.filter.CompetitionFilter;
import com.fabiocondo.repository.query.CompetitionRepositoryQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompetitionRepositoryImpl implements CompetitionRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Competition> filter(CompetitionFilter competitionFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Competition> criteria = builder.createQuery(Competition.class);
        Root<Competition> root = criteria.from(Competition.class);

        getSortOrder(competitionFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(competitionFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Competition> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(competitionFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(CompetitionFilter competitionFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Competition> root = criteria.from(Competition.class);

        Predicate[] predicates = createRestrictions(competitionFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(CompetitionFilter competitionFilter, CriteriaBuilder builder, Root<Competition> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(competitionFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(CompetitionFilter competitionFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Competition> root){

        if(!ObjectUtils.isEmpty(competitionFilter.getSearchParam())) {
            Predicate subject = builder.like(
                    builder.lower(root.get("subject").get("name")), "%" + competitionFilter.getSearchParam().toLowerCase() + "%");
            Predicate title = builder.like(
                    builder.lower(root.get("title")), "%" + competitionFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(subject, title));
        }

        if(!ObjectUtils.isEmpty(competitionFilter.getTitle())) {
            predicates.add(builder.like(
                    builder.lower(root.get("title")), "%" + competitionFilter.getTitle().toLowerCase() + "%"));
        }

        if(!ObjectUtils.isEmpty(competitionFilter.getSubject())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("subject").get("id")), competitionFilter.getSubject().getId()));
        }

        if(!ObjectUtils.isEmpty(competitionFilter.getDifficultyLevel())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("difficultyLevel")), competitionFilter.getDifficultyLevel()));
        }
    }

    public void getSortOrder(CompetitionFilter competitionFilter, CriteriaBuilder builder, CriteriaQuery<Competition> criteria, Root<Competition> root){
        if(Objects.equals(competitionFilter.getCompetitionOrderBy(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(competitionFilter.getCompetitionOrderBy(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}
