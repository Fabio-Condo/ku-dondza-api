package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.User;
import com.fabiocondo.repository.filter.UserFilter;
import com.fabiocondo.repository.query.UserRepositoryQuery;
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

public class UserRepositoryImpl implements UserRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<User> filter(UserFilter userFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<User> criteria = builder.createQuery(User.class);
        Root<User> root = criteria.from(User.class);

        getSortOrder(userFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(userFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<User> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(userFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(UserFilter userFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<User> root = criteria.from(User.class);

        Predicate[] predicates = createRestrictions(userFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(UserFilter userFilter, CriteriaBuilder builder, Root<User> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(userFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(UserFilter userFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<User> root){

        if(!ObjectUtils.isEmpty(userFilter.getSearchParam())) {
            Predicate fullName = builder.like(
                    builder.lower(root.get("fullName")), "%" + userFilter.getSearchParam().toLowerCase() + "%");
            Predicate email = builder.like(
                    builder.lower(root.get("email")), "%" + userFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(fullName, email));
        }

        if(!ObjectUtils.isEmpty(userFilter.getFullName())) {
            predicates.add(builder.like(
                    builder.lower(root.get("fullName")), "%" + userFilter.getFullName().toLowerCase() + "%"));
        }
        if(!ObjectUtils.isEmpty(userFilter.getEmail())) {
            predicates.add(builder.like(
                    builder.lower(root.get("email")), "%" + userFilter.getEmail().toLowerCase() + "%"));
        }

        if(!ObjectUtils.isEmpty(userFilter.getUserType())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("userType")), userFilter.getUserType()));
        }

        if(!ObjectUtils.isEmpty(userFilter.getRole())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("role")), userFilter.getRole()));
        }
    }

    public void getSortOrder(UserFilter userFilter, CriteriaBuilder builder, CriteriaQuery<User> criteria, Root<User> root){
        if(Objects.equals(userFilter.getSort(), "id,asc")){
            criteria.orderBy(builder.asc(root.get("id")));
        }
        if(Objects.equals(userFilter.getSort(), "id,desc")){
            criteria.orderBy(builder.desc(root.get("id")));
        }
    }
}