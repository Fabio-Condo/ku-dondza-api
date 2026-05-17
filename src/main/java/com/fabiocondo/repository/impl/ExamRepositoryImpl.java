package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Exam;
import com.fabiocondo.enumeration.ExamType;
import com.fabiocondo.enumeration.Institution;
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

        if(!ObjectUtils.isEmpty(examFilter.getSearchParam())) {
            Predicate subject = builder.like(
                    builder.lower(root.get("subject").get("name")), "%" + examFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(subject));
        }
        if(!ObjectUtils.isEmpty(examFilter.getSubject())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("subject").get("id")), examFilter.getSubject().getId()));
        }
        if (examFilter.getExamType() != null) {
            predicates.add(builder.equal(
                    builder.lower(root.get("examType")), examFilter.getExamType()));
        }
        if (examFilter.getInstitution() != null) {
            predicates.add(builder.equal(
                    builder.lower(root.get("institution")), examFilter.getInstitution()));
        }
        if (examFilter.getBeginYear() != null) {
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("year"), examFilter.getBeginYear()));
        }
        if (examFilter.getEndYear() != null) {
            predicates.add(
                    builder.lessThanOrEqualTo(root.get("year"), examFilter.getEndYear()));
        }
    }

    public void getSortOrderOLD(ExamFilter examFilter, CriteriaBuilder builder, CriteriaQuery<Exam> criteria, Root<Exam> root){
        if(Objects.equals(examFilter.getSort(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(examFilter.getSort(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }

    public void getSortOrder(ExamFilter examFilter,
                             CriteriaBuilder builder,
                             CriteriaQuery<Exam> criteria,
                             Root<Exam> root) {

        criteria.orderBy(

                // 1º: RESOLUCAO sempre no topo do seu grupo
                builder.asc(
                        builder.selectCase()
                                .when(builder.equal(root.get("examType"), ExamType.RESOLUCAO), 1)
                                .when(builder.equal(root.get("examType"), ExamType.ENUNCIADO), 2)
                                .otherwise(3)
                ),

                // 2º: Disciplina (agrupa por matéria)
                builder.asc(root.get("subject").get("name")),

                // 3º: Número do exame (agrupa enunciado + resolução)
                builder.asc(root.get("number")),

                // 4º: Ano (agrupa por ano)
                builder.desc(root.get("year")),

                // 5º: Instituição
                builder.asc(
                        builder.selectCase()
                                .when(builder.equal(root.get("institution"), Institution.UEM), 1)
                                .when(builder.equal(root.get("institution"), Institution.UP), 2)
                                .when(builder.equal(root.get("institution"), Institution.ACIPOL), 3)
                                .when(builder.equal(root.get("institution"), Institution.AM), 4)
                                .when(builder.equal(root.get("institution"), Institution.ISCAM), 5)
                                .when(builder.equal(root.get("institution"), Institution.ISCISA), 6)
                                .when(builder.equal(root.get("institution"), Institution.UJC), 7)
                                .otherwise(99)
                )
        );
    }
}