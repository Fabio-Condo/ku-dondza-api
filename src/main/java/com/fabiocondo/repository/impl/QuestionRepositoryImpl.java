package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Question;
import com.fabiocondo.repository.filter.QuestionFilter;
import com.fabiocondo.repository.query.QuestionRepositoryQuery;
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

public class QuestionRepositoryImpl implements QuestionRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Question> filter(QuestionFilter questionFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Question> criteria = builder.createQuery(Question.class);
        Root<Question> root = criteria.from(Question.class);

        getSortOrder(questionFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(questionFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Question> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(questionFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(QuestionFilter questionFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Question> root = criteria.from(Question.class);

        Predicate[] predicates = createRestrictions(questionFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(QuestionFilter questionFilter, CriteriaBuilder builder, Root<Question> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(questionFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(QuestionFilter questionFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Question> root){

        if(!ObjectUtils.isEmpty(questionFilter.getSearchParam())) {
            Predicate subject = builder.like(
                    builder.lower(root.get("subject").get("name")), "%" + questionFilter.getSearchParam().toLowerCase() + "%");
            Predicate text = builder.like(
                    builder.lower(root.get("text")), "%" + questionFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(subject, text));
        }

        if(!ObjectUtils.isEmpty(questionFilter.getText())) {
            predicates.add(builder.like(
                    builder.lower(root.get("text")), "%" + questionFilter.getText().toLowerCase() + "%"));
        }
        if(!ObjectUtils.isEmpty(questionFilter.getSubject())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("topic").get("subject").get("id")), questionFilter.getSubject().getId()));
        }
        if(!ObjectUtils.isEmpty(questionFilter.getTopic())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("topic").get("id")), questionFilter.getTopic().getId()));
        }

    }

    public void getSortOrder(QuestionFilter questionFilter, CriteriaBuilder builder, CriteriaQuery<Question> criteria, Root<Question> root){
        if(Objects.equals(questionFilter.getQuestionOrderBy(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(questionFilter.getQuestionOrderBy(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}
