package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Article;
import com.fabiocondo.repository.filter.ArticleFilter;
import com.fabiocondo.repository.query.ArticleRepositoryQuery;
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

public class ArticleRepositoryImpl implements ArticleRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Article> filter(ArticleFilter articleFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Article> criteria = builder.createQuery(Article.class);
        Root<Article> root = criteria.from(Article.class);

        getSortOrder(articleFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(articleFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Article> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(articleFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(ArticleFilter articleFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Article> root = criteria.from(Article.class);

        Predicate[] predicates = createRestrictions(articleFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(ArticleFilter articleFilter, CriteriaBuilder builder, Root<Article> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(articleFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(ArticleFilter articleFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Article> root){

        if(!ObjectUtils.isEmpty(articleFilter.getSearchParam())) {
            Predicate subject = builder.like(
                    builder.lower(root.get("subject").get("name")), "%" + articleFilter.getSearchParam().toLowerCase() + "%");
            Predicate title = builder.like(
                    builder.lower(root.get("title")), "%" + articleFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(subject, title));
        }

        if(!ObjectUtils.isEmpty(articleFilter.getTitle())) {
            predicates.add(builder.like(
                    builder.lower(root.get("title")), "%" + articleFilter.getTitle().toLowerCase() + "%"));
        }

        if(!ObjectUtils.isEmpty(articleFilter.getSubject())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("subject").get("id")), articleFilter.getSubject().getId()));
        }
    }

    public void getSortOrder(ArticleFilter articleFilter, CriteriaBuilder builder, CriteriaQuery<Article> criteria, Root<Article> root){
        if(Objects.equals(articleFilter.getArticleOrderBy(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(articleFilter.getArticleOrderBy(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}
