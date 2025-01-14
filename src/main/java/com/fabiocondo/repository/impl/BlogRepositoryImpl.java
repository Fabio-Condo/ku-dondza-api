package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Blog;
import com.fabiocondo.repository.filter.BlogFilter;
import com.fabiocondo.repository.query.BlogRepositoryQuery;
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

public class BlogRepositoryImpl implements BlogRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Blog> filter(BlogFilter blogFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Blog> criteria = builder.createQuery(Blog.class);
        Root<Blog> root = criteria.from(Blog.class);

        getSortOrder(blogFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(blogFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Blog> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(blogFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(BlogFilter blogFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Blog> root = criteria.from(Blog.class);

        Predicate[] predicates = createRestrictions(blogFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(BlogFilter blogFilter, CriteriaBuilder builder, Root<Blog> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(blogFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(BlogFilter blogFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Blog> root){

        if(!ObjectUtils.isEmpty(blogFilter.getSearchParam())) {
            Predicate subject = builder.like(
                    builder.lower(root.get("subject").get("name")), "%" + blogFilter.getSearchParam().toLowerCase() + "%");
            Predicate title = builder.like(
                    builder.lower(root.get("title")), "%" + blogFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(subject, title));
        }

        if(!ObjectUtils.isEmpty(blogFilter.getTitle())) {
            predicates.add(builder.like(
                    builder.lower(root.get("title")), "%" + blogFilter.getTitle().toLowerCase() + "%"));
        }

        if(!ObjectUtils.isEmpty(blogFilter.getSubject())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("subject").get("id")), blogFilter.getSubject().getId()));
        }
    }

    public void getSortOrder(BlogFilter blogFilter, CriteriaBuilder builder, CriteriaQuery<Blog> criteria, Root<Blog> root){
        if(Objects.equals(blogFilter.getBlogOrderBy(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(blogFilter.getBlogOrderBy(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}
