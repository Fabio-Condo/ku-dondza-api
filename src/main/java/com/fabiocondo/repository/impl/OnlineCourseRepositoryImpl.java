package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.OnlineCourse;
import com.fabiocondo.repository.filter.OnlineCourseFilter;
import com.fabiocondo.repository.query.OnlineCourseRepositoryQuery;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OnlineCourseRepositoryImpl implements OnlineCourseRepositoryQuery {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<OnlineCourse> filter(OnlineCourseFilter courseFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<OnlineCourse> criteria = builder.createQuery(OnlineCourse.class);
        Root<OnlineCourse> root = criteria.from(OnlineCourse.class);

        getSortOrder(courseFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(courseFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<OnlineCourse> query = manager.createQuery(criteria);
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

    private Long total(OnlineCourseFilter courseFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<OnlineCourse> root = criteria.from(OnlineCourse.class);

        Predicate[] predicates = createRestrictions(courseFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(OnlineCourseFilter courseFilter, CriteriaBuilder builder, Root<OnlineCourse> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(courseFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(OnlineCourseFilter courseFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<OnlineCourse> root){

        if(!ObjectUtils.isEmpty(courseFilter.getSearchParam())) {
            Predicate name = builder.like(
                    builder.lower(root.get("name")), "%" + courseFilter.getSearchParam().toLowerCase() + "%");
            Predicate instrutorFirstName = builder.like(
                    builder.lower(root.get("instrutor").get("firstName")), "%" + courseFilter.getSearchParam().toLowerCase() + "%");
            Predicate instrutorLastName = builder.like(
                    builder.lower(root.get("instrutor").get("lastName")), "%" + courseFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(name, instrutorFirstName, instrutorLastName));
        }

        if(!ObjectUtils.isEmpty(courseFilter.getName())) {
            predicates.add(builder.like(
                    builder.lower(root.get("name")), "%" + courseFilter.getName().toLowerCase() + "%"));
        }
        if (courseFilter.getInstrutor() != null) {
            predicates.add(builder.equal(
                    builder.lower(root.get("instrutor").get("id")), courseFilter.getInstrutor().getId()));
        }

        // Filtro por usuário inscrito
        // buscar cursos do usuário inscrito
        if (courseFilter.getUser() != null) {
            // Subconsulta para verificar se o usuário está na lista de students
            CriteriaQuery<OnlineCourse> criteriaQuery = builder.createQuery(OnlineCourse.class); // Cria uma nova CriteriaQuery
            Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
            Root<OnlineCourse> subRoot = subquery.from(OnlineCourse.class);
            subquery.select(subRoot.get("id"));
            subquery.where(builder.equal(subRoot.join("students").get("id"), courseFilter.getUser().getId()));

            // Adiciona a condição à consulta principal
            predicates.add(root.get("id").in(subquery));
        }
    }

    public void getSortOrder(OnlineCourseFilter examFilter, CriteriaBuilder builder, CriteriaQuery<OnlineCourse> criteria, Root<OnlineCourse> root){
        if(Objects.equals(examFilter.getCourseOrderBy(), "id,asc")){
            criteria.orderBy(builder.asc(root.get("id")));
        }
        if(Objects.equals(examFilter.getCourseOrderBy(), "id,desc")){
            criteria.orderBy(builder.desc(root.get("id")));
        }
    }
}
