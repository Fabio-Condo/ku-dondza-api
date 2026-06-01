package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Topic;
import com.fabiocondo.repository.filter.TopicFilter;
import com.fabiocondo.repository.query.TopicRepositoryQuery;
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
import java.util.*;

public class TopicRepositoryImpl implements TopicRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Topic> filter(TopicFilter topicFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Topic> criteria = builder.createQuery(Topic.class);
        Root<Topic> root = criteria.from(Topic.class);

        getSortOrder(topicFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(topicFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Topic> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(topicFilter));
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(TopicFilter topicFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Topic> root = criteria.from(Topic.class);

        Predicate[] predicates = createRestrictions(topicFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(TopicFilter topicFilter, CriteriaBuilder builder, Root<Topic> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(topicFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(TopicFilter topicFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Topic> root){

        if(!ObjectUtils.isEmpty(topicFilter.getSearchParam())) {
            Predicate subject = builder.like(
                    builder.lower(root.get("subject").get("name")), "%" + topicFilter.getSearchParam().toLowerCase() + "%");
            Predicate topic = builder.like(
                    builder.lower(root.get("name")), "%" + topicFilter.getSearchParam().toLowerCase() + "%");

            predicates.add(builder.or(subject, topic));
        }

        if(!ObjectUtils.isEmpty(topicFilter.getName())) {
            predicates.add(builder.like(
                    builder.lower(root.get("name")), "%" + topicFilter.getName().toLowerCase() + "%"));
        }
        if(!ObjectUtils.isEmpty(topicFilter.getSubjectId())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("subject").get("id")), topicFilter.getSubjectId()));
        }

        // Adiciona filtro para enabled = true
        predicates.add(builder.isTrue(root.get("enabled")));
    }

    public void getSortOrder(TopicFilter topicFilter, CriteriaBuilder builder, CriteriaQuery<Topic> criteria, Root<Topic> root){
        if(Objects.equals(topicFilter.getSort(), "id,asc")){
            criteria.orderBy(builder.asc(root.get("id")));
        }
        if(Objects.equals(topicFilter.getSort(), "id,desc")){
            criteria.orderBy(builder.desc(root.get("id")));
        }
        if(Objects.equals(topicFilter.getSort(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(topicFilter.getSort(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}
