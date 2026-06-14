package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.FlashCard;
import com.fabiocondo.repository.filter.FlashCardFilter;
import com.fabiocondo.repository.query.FlashCardRepositoryQuery;
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
import java.util.*;

public class FlashCardRepositoryImpl implements FlashCardRepositoryQuery {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<FlashCard> filter(FlashCardFilter flashCardFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<FlashCard> criteria = builder.createQuery(FlashCard.class);
        Root<FlashCard> root = criteria.from(FlashCard.class);

        getSortOrder(flashCardFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(flashCardFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<FlashCard> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(flashCardFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(FlashCardFilter flashCardFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<FlashCard> root = criteria.from(FlashCard.class);

        Predicate[] predicates = createRestrictions(flashCardFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(FlashCardFilter flashCardFilter, CriteriaBuilder builder, Root<FlashCard> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(flashCardFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(FlashCardFilter flashCardFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<FlashCard> root){

        if(!ObjectUtils.isEmpty(flashCardFilter.getSubjectId())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("topic").get("subject").get("id")), flashCardFilter.getSubjectId()));
        }
        if(!ObjectUtils.isEmpty(flashCardFilter.getTopicId())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("topic").get("id")), flashCardFilter.getTopicId()));
        }
    }

    public void getSortOrder(FlashCardFilter flashCardFilter, CriteriaBuilder builder, CriteriaQuery<FlashCard> criteria, Root<FlashCard> root){
        if(Objects.equals(flashCardFilter.getSort(), "id,asc")){
            criteria.orderBy(builder.asc(root.get("id")));
        }
        if(Objects.equals(flashCardFilter.getSort(), "id,desc")){
            criteria.orderBy(builder.desc(root.get("id")));
        }
        if(Objects.equals(flashCardFilter.getSort(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(flashCardFilter.getSort(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}

