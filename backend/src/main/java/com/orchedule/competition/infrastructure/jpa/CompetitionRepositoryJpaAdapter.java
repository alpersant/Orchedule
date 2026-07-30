package com.orchedule.competition.infrastructure.jpa;

import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
import com.orchedule.competition.domain.CompetitionStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CompetitionRepositoryJpaAdapter implements CompetitionRepository {

    private final SpringDataCompetitionRepository springDataCompetitionRepository;

    public CompetitionRepositoryJpaAdapter(SpringDataCompetitionRepository springDataCompetitionRepository) {
        this.springDataCompetitionRepository = springDataCompetitionRepository;
    }

    @Override
    public Competition save(Competition competition) {
        CompetitionEntity entity = new CompetitionEntity(
                competition.getId(), competition.getName(), competition.getDescription(),
                competition.getStatus(), competition.getDefaultDays(), competition.getDefaultHours(),
                competition.getDefaultFieldCount(), competition.getPreferencePolicy(),
                competition.getCreatedAt(), competition.getUpdatedAt());
        CompetitionEntity saved = springDataCompetitionRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Competition> findById(UUID id) {
        return springDataCompetitionRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Competition> findAll() {
        return springDataCompetitionRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Competition> findByStatus(CompetitionStatus status) {
        return springDataCompetitionRepository.findByStatus(status).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return springDataCompetitionRepository.existsByName(name);
    }

    private Competition toDomain(CompetitionEntity entity) {
        return new Competition(entity.getId(), entity.getName(), entity.getDescription(),
                entity.getStatus(), entity.getDefaultDays(), entity.getDefaultHours(),
                entity.getDefaultFieldCount(), entity.getPreferencePolicy(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
