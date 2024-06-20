package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Exame;
import com.fabiocondo.repository.filter.ExameFilter;
import com.fabiocondo.repository.query.ExameRepositoryQuery;
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

public class ExameRepositoryImpl implements ExameRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Exame> filter(ExameFilter exameFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Exame> criteria = builder.createQuery(Exame.class);
        Root<Exame> root = criteria.from(Exame.class);

        getSortOrder(exameFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(exameFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Exame> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(exameFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(ExameFilter exameFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Exame> root = criteria.from(Exame.class);

        Predicate[] predicates = createRestrictions(exameFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(ExameFilter exameFilter, CriteriaBuilder builder,Root<Exame> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(exameFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(ExameFilter exameFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Exame> root){

        if(!ObjectUtils.isEmpty(exameFilter.getGlobal())) {
            Predicate subject = builder.like(
                    builder.lower(root.get("subject")), "%" + exameFilter.getGlobal().toLowerCase() + "%");
            Predicate description = builder.like(
                    builder.lower(root.get("description")), "%" + exameFilter.getGlobal().toLowerCase() + "%");
            Predicate institutionType = builder.like(
                    builder.lower(root.get("institution").get("type")), "%" + exameFilter.getGlobal().toLowerCase() + "%");
            Predicate institutionName = builder.like(
                    builder.lower(root.get("institution").get("name")), "%" + exameFilter.getGlobal().toLowerCase() + "%");
            Predicate institutionAcronym = builder.like(
                    builder.lower(root.get("institution").get("acronym")), "%" + exameFilter.getGlobal().toLowerCase() + "%");
            predicates.add(builder.or(subject, description, institutionType, institutionName, institutionAcronym));
        }

        if(!ObjectUtils.isEmpty(exameFilter.getDescription())) {
            predicates.add(builder.like(
                    builder.lower(root.get("description")), "%" + exameFilter.getDescription().toLowerCase() + "%"));
        }
        if(!ObjectUtils.isEmpty(exameFilter.getSubject())) {
            predicates.add(builder.like(
                    builder.lower(root.get("subject")), "%" + exameFilter.getSubject().toLowerCase() + "%"));
        }
        if (exameFilter.getInstitution() != null) {
            predicates.add(builder.equal(
                    builder.lower(root.get("institution").get("id")), exameFilter.getInstitution().getId()));
        }
        if (exameFilter.getBeginDate() != null) {
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("date"), exameFilter.getBeginDate()));
        }
        if (exameFilter.getEndDate() != null) {
            predicates.add(
                    builder.lessThanOrEqualTo(root.get("date"), exameFilter.getEndDate()));
        }
    }

    public void getSortOrder(ExameFilter exameFilter, CriteriaBuilder builder, CriteriaQuery<Exame> criteria, Root<Exame> root){
        if(Objects.equals(exameFilter.getExameOrderBy(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(exameFilter.getExameOrderBy(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}
