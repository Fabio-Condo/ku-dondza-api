package com.fabiocondo.repository.impl;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.repository.filter.ChallengeFilter;
import com.fabiocondo.repository.query.ChallengeRepositoryQuery;
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
import java.util.Date;
import java.util.List;

public class ChallengeRepositoryImpl implements ChallengeRepositoryQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Challenge> filter(
            ChallengeFilter challengeFilter,
            Pageable pageable
    ) {

        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Challenge> criteria = builder.createQuery(Challenge.class);

        Root<Challenge> root = criteria.from(Challenge.class);

        Predicate[] predicates = createRestrictions(
                challengeFilter,
                builder,
                root
        );

        criteria.where(predicates);

        applySort(
                challengeFilter,
                builder,
                criteria,
                root
        );

        TypedQuery<Challenge> query = manager.createQuery(criteria);

        addRestrictionsPagination(query, pageable);

        return new PageImpl<>(
                query.getResultList(),
                pageable,
                total(challengeFilter)
        );
    }

    private void addRestrictionsPagination(
            TypedQuery<?> query,
            Pageable pageable
    ) {

        int currentPage = pageable.getPageNumber();
        int totalRecordsByPage = pageable.getPageSize();
        int firstPageRecord = currentPage * totalRecordsByPage;

        query.setFirstResult(firstPageRecord);
        query.setMaxResults(totalRecordsByPage);

        logger.info(
                "Current page: {} | Records per page: {} | First record: {}",
                currentPage,
                totalRecordsByPage,
                firstPageRecord
        );
    }

    private Long total(ChallengeFilter challengeFilter) {

        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);

        Root<Challenge> root = criteria.from(Challenge.class);

        Predicate[] predicates = createRestrictions(
                challengeFilter,
                builder,
                root
        );

        criteria.where(predicates);

        criteria.select(builder.count(root));

        return manager.createQuery(criteria).getSingleResult();
    }

    private Predicate[] createRestrictions(
            ChallengeFilter filter,
            CriteriaBuilder builder,
            Root<Challenge> root
    ) {

        List<Predicate> predicates = new ArrayList<>();

        // FILTRO POR TÍTULO
        if (!ObjectUtils.isEmpty(filter.getTitle())) {
            predicates.add(
                    builder.like(
                            builder.lower(root.get("title")),
                            "%" + filter.getTitle().toLowerCase() + "%"
                    )
            );
        }

        // FILTRO POR DESCRIÇÃO
        if (!ObjectUtils.isEmpty(filter.getDescription())) {
            predicates.add(
                    builder.like(
                            builder.lower(root.get("description")),
                            "%" + filter.getDescription().toLowerCase() + "%"
                    )
            );
        }

        // FILTRO POR NÍVEL DE DIFICULDADE
        if (filter.getDifficultyLevel() != null) {
            predicates.add(
                    builder.equal(
                            root.get("difficultyLevel"),
                            filter.getDifficultyLevel()
                    )
            );
        }

        // FILTRO POR DISCIPLINA
        if (filter.getSubject() != null &&
                filter.getSubject().getId() != null) {

            predicates.add(
                    builder.equal(
                            root.get("subject").get("id"),
                            filter.getSubject().getId()
                    )
            );
        }

        // FILTRO POR DATA INICIAL
        if (filter.getStartDate() != null) {
            predicates.add(
                    builder.greaterThanOrEqualTo(
                            root.get("startDate"),
                            filter.getStartDate()
                    )
            );
        }

        // FILTRO POR DATA FINAL
        if (filter.getEndDate() != null) {
            predicates.add(
                    builder.lessThanOrEqualTo(
                            root.get("endDate"),
                            filter.getEndDate()
                    )
            );
        }

        // FILTRO POR STATUS
        Date now = new Date();

        if (!ObjectUtils.isEmpty(filter.getStatus())) {

            switch (filter.getStatus()) {

                case "UPCOMING":
                    // ainda não começou
                    predicates.add(
                            builder.greaterThan(
                                    root.get("startDate"),
                                    now
                            )
                    );
                    break;

                case "ONGOING":
                    // já começou e ainda não terminou
                    predicates.add(
                            builder.lessThanOrEqualTo(
                                    root.get("startDate"),
                                    now
                            )
                    );

                    predicates.add(
                            builder.greaterThanOrEqualTo(
                                    root.get("endDate"),
                                    now
                            )
                    );
                    break;

                case "DONE":
                    // já terminou
                    predicates.add(
                            builder.lessThan(
                                    root.get("endDate"),
                                    now
                            )
                    );
                    break;
            }
        }

        return predicates.toArray(new Predicate[0]);
    }

    private void applySort(
            ChallengeFilter filter,
            CriteriaBuilder builder,
            CriteriaQuery<Challenge> criteria,
            Root<Challenge> root
    ) {

        if (ObjectUtils.isEmpty(filter.getSort())) {
            criteria.orderBy(
                    builder.desc(root.get("id"))
            );
            return;
        }

        String[] sortParts = filter.getSort().split(",");

        String field = sortParts[0];

        String direction = sortParts.length > 1
                ? sortParts[1]
                : "asc";

        if ("desc".equalsIgnoreCase(direction)) {
            criteria.orderBy(
                    builder.desc(root.get(field))
            );
        } else {
            criteria.orderBy(
                    builder.asc(root.get(field))
            );
        }
    }
}