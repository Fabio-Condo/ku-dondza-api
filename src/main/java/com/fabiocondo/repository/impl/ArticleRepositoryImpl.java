package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Article;
import com.fabiocondo.domain.User;
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
import javax.persistence.criteria.*;
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
            Predicate title = builder.like(
                    builder.lower(root.get("title")), "%" + articleFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(title));
        }

        if(!ObjectUtils.isEmpty(articleFilter.getTitle())) {
            predicates.add(builder.like(
                    builder.lower(root.get("title")), "%" + articleFilter.getTitle().toLowerCase() + "%"));
        }

        if(!ObjectUtils.isEmpty(articleFilter.getCategory())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("category")), articleFilter.getCategory()));
        }

        // Filtro: artigos salvos por um usuário específico
        if (articleFilter.getUserId() != null) {
            // Criação da subquery
            Subquery<Long> subquery = builder.createQuery().subquery(Long.class);
            Root<User> userRoot = subquery.from(User.class);

            // Referência à coleção savedArticles
            Join<User, Article> savedArticlesJoin = userRoot.join("savedArticles");

            // Subquery retorna os IDs dos artigos salvos por esse usuário
            subquery.select(savedArticlesJoin.get("id"))
                    .where(builder.equal(userRoot.get("id"), articleFilter.getUserId()));

            // Restringe o artigo atual (root) aos IDs retornados na subquery
            predicates.add(root.get("id").in(subquery));
        }
    }

    public void getSortOrder(ArticleFilter articleFilter, CriteriaBuilder builder, CriteriaQuery<Article> criteria, Root<Article> root){
        if(Objects.equals(articleFilter.getSort(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(articleFilter.getSort(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}
