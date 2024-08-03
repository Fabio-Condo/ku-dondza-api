package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Institution;
import com.fabiocondo.repository.filter.InstitutionFilter;
import com.fabiocondo.repository.query.InstitutionRepositoryQuery;
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

public class InstitutionRepositoryImpl implements InstitutionRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Institution> filter(InstitutionFilter institutionFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Institution> criteria = builder.createQuery(Institution.class);
        Root<Institution> root = criteria.from(Institution.class);

        getSortOrder(institutionFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(institutionFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Institution> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(institutionFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(InstitutionFilter institutionFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Institution> root = criteria.from(Institution.class);

        Predicate[] predicates = createRestrictions(institutionFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(InstitutionFilter institutionFilter, CriteriaBuilder builder,Root<Institution> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(institutionFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(InstitutionFilter institutionFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Institution> root){

        if(!ObjectUtils.isEmpty(institutionFilter.getGlobal())) {
            Predicate name = builder.like(
                    builder.lower(root.get("name")), "%" + institutionFilter.getGlobal().toLowerCase() + "%");
            Predicate type = builder.like(
                    builder.lower(root.get("type")), "%" + institutionFilter.getGlobal().toLowerCase() + "%");
            Predicate acronym = builder.like(
                    builder.lower(root.get("acronym")), "%" + institutionFilter.getGlobal().toLowerCase() + "%");
            predicates.add(builder.or(name, type, acronym));
        }
        if(!ObjectUtils.isEmpty(institutionFilter.getAdministrationType())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("administrationType")), institutionFilter.getAdministrationType()));
        }
        if(!ObjectUtils.isEmpty(institutionFilter.getCountry())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("country")), institutionFilter.getCountry()));
        }
        if(!ObjectUtils.isEmpty(institutionFilter.getType())) {
            predicates.add(builder.like(
                    builder.lower(root.get("type")), "%" + institutionFilter.getType().toLowerCase() + "%"));
        }
        if(!ObjectUtils.isEmpty(institutionFilter.getName())) {
            predicates.add(builder.like(
                    builder.lower(root.get("name")), "%" + institutionFilter.getName().toLowerCase() + "%"));
        }
    }

    public void getSortOrder(InstitutionFilter institutionFilter, CriteriaBuilder builder, CriteriaQuery<Institution> criteria, Root<Institution> root){
        if(Objects.equals(institutionFilter.getInstitutionOrderBy(), "name,asc")){
            criteria.orderBy(builder.asc(root.get("name")));
        }
        if(Objects.equals(institutionFilter.getInstitutionOrderBy(), "name,desc")){
            criteria.orderBy(builder.desc(root.get("name")));
        }
    }
}
