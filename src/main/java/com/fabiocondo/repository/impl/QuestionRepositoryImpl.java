package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.User;
import com.fabiocondo.enumeration.DifficultyLevel;
import com.fabiocondo.repository.filter.QuestionFilter;
import com.fabiocondo.repository.query.QuestionRepositoryQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;

public class QuestionRepositoryImpl implements QuestionRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Question> filter(QuestionFilter questionFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Question> criteria = builder.createQuery(Question.class);
        Root<Question> root = criteria.from(Question.class);

        getSortOrder(questionFilter, builder, criteria, root);

        Predicate[] predicates = createRestrictions(questionFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Question> query = manager.createQuery(criteria);
        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(questionFilter));
    }

    @Override
    public Set<Question> findRandomQuestionsByTopicsAndDifficulty(Set<Long> topicIds, DifficultyLevel difficultyLevel, int limitPerTopic) {

        // Essa query utiliza CTE e ROW_NUMBER para particionar as questões por tópico,
        // ordenando aleatoriamente (usando RAND() para MySQL; se for PostgreSQL, substitua por RANDOM())
        String sql = "WITH ranked_questions AS ( " +
                "    SELECT q.*, ROW_NUMBER() OVER (PARTITION BY q.topic_id ORDER BY RAND()) as rn " +
                "    FROM question q " +
                "    WHERE q.topic_id IN (:topicIds) AND q.difficulty_level = :difficultyLevel " +
                ") " +
                "SELECT * FROM ranked_questions WHERE rn <= :limitPerTopic";

        Query query = manager.createNativeQuery(sql, Question.class);
        query.setParameter("topicIds", topicIds);
        query.setParameter("difficultyLevel", difficultyLevel.name()); // ajuste conforme o mapeamento do enum
        query.setParameter("limitPerTopic", limitPerTopic);

        List<Question> questions = query.getResultList();
        return new HashSet<>(questions);
    }

    private void addRestrictionsPagination(TypedQuery<?> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info("Current page: " + currentPage + " Total records by page: " + totalRecordsByPage + " First record page " + firstPageRecord);
    }

    private Long total(QuestionFilter questionFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Question> root = criteria.from(Question.class);

        Predicate[] predicates = createRestrictions(questionFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(QuestionFilter questionFilter, CriteriaBuilder builder, Root<Question> root) {
        List<Predicate> predicates = new ArrayList<>();

        restrictions(questionFilter, predicates, builder, root);

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    public void restrictions(QuestionFilter questionFilter, List<Predicate> predicates, CriteriaBuilder builder, Root<Question> root){

        if(!ObjectUtils.isEmpty(questionFilter.getText())) {
            predicates.add(builder.like(
                    builder.lower(root.get("text")), "%" + questionFilter.getText().toLowerCase() + "%"));
        }
        if(!ObjectUtils.isEmpty(questionFilter.getSubject())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("topic").get("subject").get("id")), questionFilter.getSubject().getId()));
        }
        if(!ObjectUtils.isEmpty(questionFilter.getTopic())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("topic").get("id")), questionFilter.getTopic().getId()));
        }
        if(!ObjectUtils.isEmpty(questionFilter.getDifficultyLevel())) {
            predicates.add(builder.equal(
                    builder.lower(root.get("difficultyLevel")), questionFilter.getDifficultyLevel()));
        }

        // Filtro: questions salvos por um usuário específico
        if (questionFilter.getUserId() != null) {
            // Criação da subquery
            Subquery<Long> subquery = builder.createQuery().subquery(Long.class);
            Root<User> userRoot = subquery.from(User.class);

            // Referência à coleção savedQuestions
            Join<User, Question> savedQuestionsJoin = userRoot.join("savedQuestions");

            // Subquery retorna os IDs dos questions salvos por esse usuário
            subquery.select(savedQuestionsJoin.get("id"))
                    .where(builder.equal(userRoot.get("id"), questionFilter.getUserId()));

            // Restringe o question atual (root) aos IDs retornados na subquery
            predicates.add(root.get("id").in(subquery));
        }

    }

    public void getSortOrder(QuestionFilter questionFilter, CriteriaBuilder builder, CriteriaQuery<Question> criteria, Root<Question> root){
        if(Objects.equals(questionFilter.getSort(), "id,asc")){
            criteria.orderBy(builder.asc(root.get("id")));
        }
        if(Objects.equals(questionFilter.getSort(), "id,desc")){
            criteria.orderBy(builder.desc(root.get("id")));
        }
        if(Objects.equals(questionFilter.getSort(), "subject,asc")){
            criteria.orderBy(builder.asc(root.get("subject")));
        }
        if(Objects.equals(questionFilter.getSort(), "subject,desc")){
            criteria.orderBy(builder.desc(root.get("subject")));
        }
    }
}
