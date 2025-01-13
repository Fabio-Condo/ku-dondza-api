package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Book;
import com.fabiocondo.repository.filter.BookFilter;
import com.fabiocondo.repository.query.BookRepositoryQuery;
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

public class BookRepositoryImpl implements BookRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Book> filter(BookFilter bookFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Book> criteria = builder.createQuery(Book.class);
        Root<Book> root = criteria.from(Book.class);

        getSortOrder(bookFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(bookFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Book> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(bookFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(BookFilter bookFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Book> root = criteria.from(Book.class);

        Predicate[] predicates = createRestrictions(bookFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(BookFilter bookFilter, CriteriaBuilder builder, Root<Book> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(bookFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(BookFilter bookFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Book> root){

        if(!ObjectUtils.isEmpty(bookFilter.getSearchParam())) {
            Predicate subject = builder.like(
                    builder.lower(root.get("subject").get("name")), "%" + bookFilter.getSearchParam().toLowerCase() + "%");
            Predicate description = builder.like(
                    builder.lower(root.get("description")), "%" + bookFilter.getSearchParam().toLowerCase() + "%");
            Predicate name = builder.like(
                    builder.lower(root.get("name")), "%" + bookFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(subject, description, name));
        }

        if(!ObjectUtils.isEmpty(bookFilter.getName())) {
            predicates.add(builder.like(
                    builder.lower(root.get("name")), "%" + bookFilter.getName().toLowerCase() + "%"));
        }
        if(!ObjectUtils.isEmpty(bookFilter.getDescription())) {
            predicates.add(builder.like(
                    builder.lower(root.get("description")), "%" + bookFilter.getDescription().toLowerCase() + "%"));
        }
        if(!ObjectUtils.isEmpty(bookFilter.getSubject())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("subject").get("id")), bookFilter.getSubject().getId()));
        }
    }

    public void getSortOrder(BookFilter bookFilter, CriteriaBuilder builder, CriteriaQuery<Book> criteria, Root<Book> root){
        if(Objects.equals(bookFilter.getExameOrderBy(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(bookFilter.getExameOrderBy(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}
