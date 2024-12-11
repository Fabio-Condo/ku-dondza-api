package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Course;
import com.fabiocondo.repository.filter.CourseFilter;
import com.fabiocondo.repository.query.CourseRepositoryQuery;
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

public class CourseRepositoryImpl implements CourseRepositoryQuery {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Course> filter(CourseFilter courseFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Course> criteria = builder.createQuery(Course.class);
        Root<Course> root = criteria.from(Course.class);

        getSortOrder(courseFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(courseFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Course> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(courseFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(CourseFilter courseFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Course> root = criteria.from(Course.class);

        Predicate[] predicates = createRestrictions(courseFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(CourseFilter courseFilter, CriteriaBuilder builder, Root<Course> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(courseFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(CourseFilter courseFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Course> root){

        if(!ObjectUtils.isEmpty(courseFilter.getSearchParam())) {
            Predicate name = builder.like(
                    builder.lower(root.get("name")), "%" + courseFilter.getSearchParam().toLowerCase() + "%");
            Predicate institutionType = builder.like(
                    builder.lower(root.get("institution").get("type")), "%" + courseFilter.getSearchParam().toLowerCase() + "%");
            Predicate institutionName = builder.like(
                    builder.lower(root.get("institution").get("name")), "%" + courseFilter.getSearchParam().toLowerCase() + "%");
            Predicate institutionAcronym = builder.like(
                    builder.lower(root.get("institution").get("acronym")), "%" + courseFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(name, institutionType, institutionName, institutionAcronym));
        }

        if(!ObjectUtils.isEmpty(courseFilter.getName())) {
            predicates.add(builder.like(
                    builder.lower(root.get("name")), "%" + courseFilter.getName().toLowerCase() + "%"));
        }
        if (courseFilter.getInstitution() != null) {
            predicates.add(builder.equal(
                    builder.lower(root.get("institution").get("id")), courseFilter.getInstitution().getId()));
        }
    }

    public void getSortOrder(CourseFilter examFilter, CriteriaBuilder builder, CriteriaQuery<Course> criteria, Root<Course> root){
        if(Objects.equals(examFilter.getCourseOrderBy(), "id,asc")){
            criteria.orderBy(builder.asc(root.get("id")));
        }
        if(Objects.equals(examFilter.getCourseOrderBy(), "id,desc")){
            criteria.orderBy(builder.desc(root.get("id")));
        }
    }
}
