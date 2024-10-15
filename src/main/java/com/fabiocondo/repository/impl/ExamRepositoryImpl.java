package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Exam;
import com.fabiocondo.repository.filter.ExamFilter;
import com.fabiocondo.repository.query.ExamRepositoryQuery;
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

public class ExamRepositoryImpl implements ExamRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Exam> filter(ExamFilter examFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Exam> criteria = builder.createQuery(Exam.class);
        Root<Exam> root = criteria.from(Exam.class);

        getSortOrder(examFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(examFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Exam> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(examFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(ExamFilter examFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Exam> root = criteria.from(Exam.class);

        Predicate[] predicates = createRestrictions(examFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(ExamFilter examFilter, CriteriaBuilder builder, Root<Exam> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(examFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(ExamFilter examFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Exam> root){

        if(!ObjectUtils.isEmpty(examFilter.getGlobal())) {
            Predicate subject = builder.like(
                    builder.lower(root.get("subject").get("name")), "%" + examFilter.getGlobal().toLowerCase() + "%");
            Predicate description = builder.like(
                    builder.lower(root.get("description")), "%" + examFilter.getGlobal().toLowerCase() + "%");
            Predicate institutionType = builder.like(
                    builder.lower(root.get("institution").get("type")), "%" + examFilter.getGlobal().toLowerCase() + "%");
            Predicate institutionName = builder.like(
                    builder.lower(root.get("institution").get("name")), "%" + examFilter.getGlobal().toLowerCase() + "%");
            Predicate institutionAcronym = builder.like(
                    builder.lower(root.get("institution").get("acronym")), "%" + examFilter.getGlobal().toLowerCase() + "%");
            predicates.add(builder.or(subject, description, institutionType, institutionName, institutionAcronym));
        }

        if(!ObjectUtils.isEmpty(examFilter.getDescription())) {
            predicates.add(builder.like(
                    builder.lower(root.get("description")), "%" + examFilter.getDescription().toLowerCase() + "%"));
        }
        if(!ObjectUtils.isEmpty(examFilter.getSubject())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("subject").get("id")), examFilter.getSubject().getId()));
        }
        if (examFilter.getInstitution() != null) {
            predicates.add(builder.equal(
                    builder.lower(root.get("institution").get("id")), examFilter.getInstitution().getId()));
        }
        if (examFilter.getBeginDate() != null) {
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("date"), examFilter.getBeginDate()));
        }
        if (examFilter.getEndDate() != null) {
            predicates.add(
                    builder.lessThanOrEqualTo(root.get("date"), examFilter.getEndDate()));
        }
    }

    public void getSortOrder(ExamFilter examFilter, CriteriaBuilder builder, CriteriaQuery<Exam> criteria, Root<Exam> root){
        if(Objects.equals(examFilter.getExameOrderBy(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(examFilter.getExameOrderBy(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}
