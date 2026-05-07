package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.repository.filter.ChallengeFilter;
import com.fabiocondo.repository.query.ChallengeRepositoryQuery;
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

public class ChallengeRepositoryImpl implements ChallengeRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Challenge> filter(ChallengeFilter challengeFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Challenge> criteria = builder.createQuery(Challenge.class);
        Root<Challenge> root = criteria.from(Challenge.class);

        getSortOrder(challengeFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(challengeFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Challenge> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(challengeFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(ChallengeFilter challengeFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Challenge> root = criteria.from(Challenge.class);

        Predicate[] predicates = createRestrictions(challengeFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(ChallengeFilter challengeFilter, CriteriaBuilder builder, Root<Challenge> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(challengeFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(ChallengeFilter challengeFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Challenge> root){
        if(!ObjectUtils.isEmpty(challengeFilter.getSubject())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("subject").get("id")), challengeFilter.getSubject().getId()));
        }
    }

    public void getSortOrder(ChallengeFilter challengeFilter, CriteriaBuilder builder, CriteriaQuery<Challenge> criteria, Root<Challenge> root){
        if(Objects.equals(challengeFilter.getSubject(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(challengeFilter.getSubject(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}