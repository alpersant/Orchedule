package com.orchedule.competition.infrastructure.jpa;

import com.orchedule.competition.domain.CompetitionStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class CompetitionSpecifications {

    public Specification<CompetitionEntity> byStatusAndName(
            CompetitionStatus status,
            String search
    ) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (status != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("status"), status)
                );
            }

            if (search != null && !search.isBlank()) {
                String queryValue = "%" + search.trim().toLowerCase() + "%";

                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                queryValue
                        )
                );
            }

            return predicate;
        };
    }
}