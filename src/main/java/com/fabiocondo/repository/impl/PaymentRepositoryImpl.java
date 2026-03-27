package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Payment;
import com.fabiocondo.repository.filter.PaymentFilter;
import com.fabiocondo.repository.query.PaymentRepositoryQuery;
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

public class PaymentRepositoryImpl implements PaymentRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Payment> filter(PaymentFilter paymentFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Payment> criteria = builder.createQuery(Payment.class);
        Root<Payment> root = criteria.from(Payment.class);

        getSortOrder(paymentFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(paymentFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Payment> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(paymentFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(PaymentFilter paymentFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Payment> root = criteria.from(Payment.class);

        Predicate[] predicates = createRestrictions(paymentFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(PaymentFilter paymentFilter, CriteriaBuilder builder, Root<Payment> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(paymentFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(PaymentFilter paymentFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Payment> root){

        if(!ObjectUtils.isEmpty(paymentFilter.getSearchParam())) {
            Predicate subject = builder.like(
                    builder.lower(root.get("user").get("fullName")), "%" + paymentFilter.getSearchParam().toLowerCase() + "%");
            predicates.add(builder.or(subject));
        }

        if (paymentFilter.getStatus() != null) {
            predicates.add(builder.equal(
                    builder.lower(root.get("status")), paymentFilter.getStatus()));
        }
    }

    public void getSortOrder(PaymentFilter paymentFilter, CriteriaBuilder builder, CriteriaQuery<Payment> criteria, Root<Payment> root){
        if(Objects.equals(paymentFilter.getPaymentOrderBy(), "user,asc")){
            criteria.orderBy(builder.asc(root.get("user")));
        }
        if(Objects.equals(paymentFilter.getPaymentOrderBy(), "user,desc")){
            criteria.orderBy(builder.desc(root.get("user")));
        }
    }
}