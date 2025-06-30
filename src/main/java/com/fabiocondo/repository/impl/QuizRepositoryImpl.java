package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Quiz;
import com.fabiocondo.repository.filter.QuizFilter;
import com.fabiocondo.repository.query.QuizRepositoryQuery;
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

public class QuizRepositoryImpl implements QuizRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Quiz> filter(QuizFilter quizFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Quiz> criteria = builder.createQuery(Quiz.class);
        Root<Quiz> root = criteria.from(Quiz.class);

        getSortOrder(quizFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(quizFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Quiz> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(quizFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(QuizFilter quizFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Quiz> root = criteria.from(Quiz.class);

        Predicate[] predicates = createRestrictions(quizFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(QuizFilter quizFilter, CriteriaBuilder builder, Root<Quiz> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(quizFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(QuizFilter quizFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Quiz> root){

        if(!ObjectUtils.isEmpty(quizFilter.getSearchParam())) {
            Predicate subject = builder.like(
                    builder.lower(root.get("subject").get("name")), "%" + quizFilter.getSearchParam().toLowerCase() + "%");
            Predicate title = builder.like(
                    builder.lower(root.get("title")), "%" + quizFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(subject, title));
        }

        if(!ObjectUtils.isEmpty(quizFilter.getSubject())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("subject").get("id")), quizFilter.getSubject().getId()));
        }

        if(!ObjectUtils.isEmpty(quizFilter.getUser())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("user").get("id")), quizFilter.getUser().getId()));
        }

        if(!ObjectUtils.isEmpty(quizFilter.getDifficultyLevel())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("difficultyLevel")), quizFilter.getDifficultyLevel()));
        }
    }

    public void getSortOrder(QuizFilter quizFilter, CriteriaBuilder builder, CriteriaQuery<Quiz> criteria, Root<Quiz> root){
        if(Objects.equals(quizFilter.getSort(), "id,asc")){
            criteria.orderBy(builder.asc(root.get("id")));
        }
        if(Objects.equals(quizFilter.getSort(), "id,desc")){
            criteria.orderBy(builder.desc(root.get("id")));
        }
        if(Objects.equals(quizFilter.getSort(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(quizFilter.getSort(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}
