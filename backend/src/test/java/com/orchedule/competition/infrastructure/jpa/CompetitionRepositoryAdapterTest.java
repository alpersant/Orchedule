package com.orchedule.competition.infrastructure.jpa;

import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionDay;
import com.orchedule.competition.domain.CompetitionHour;
import com.orchedule.competition.domain.CompetitionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CompetitionRepositoryAdapterTest {

    @Autowired
    private SpringDataCompetitionRepository springDataCompetitionRepository;

    private CompetitionRepositoryJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CompetitionRepositoryJpaAdapter(springDataCompetitionRepository);
    }

    @Test
    void shouldSaveAndRetrieveCompetitionWithDefaultsById() {
        Competition competition = Competition.createWithDefaults("Liga Local", "Desc");

        Competition saved = adapter.save(competition);
        Optional<Competition> found = adapter.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Liga Local");
        assertThat(found.get().getDefaultDays()).isEqualTo(Competition.DEFAULT_DAYS);
        assertThat(found.get().getDefaultHours()).isEqualTo(Competition.DEFAULT_HOURS);
    }

    @Test
    void shouldPersistCustomDaysAndHours() {
        Competition competition = Competition.createWithDefaults("Weekend League", "Desc");
        competition.updateDefaultDays(Set.of(CompetitionDay.SATURDAY, CompetitionDay.SUNDAY));
        competition.updateDefaultHours(Set.of(CompetitionHour.H20));

        Competition saved = adapter.save(competition);
        Optional<Competition> found = adapter.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getDefaultDays())
                .containsExactlyInAnyOrder(CompetitionDay.SATURDAY, CompetitionDay.SUNDAY);
        assertThat(found.get().getDefaultHours()).containsExactly(CompetitionHour.H20);
    }

    @Test
    void shouldFindAllCompetitions() {
        adapter.save(Competition.createWithDefaults("Liga A", "Desc"));
        adapter.save(Competition.createWithDefaults("Liga B", "Desc"));

        List<Competition> result = adapter.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldFindCompetitionsByStatus() {
        Competition draft = Competition.createWithDefaults("Draft Liga", "Desc");
        Competition active = Competition.createWithDefaults("Active Liga", "Desc");
        active.activate();

        adapter.save(draft);
        adapter.save(active);

        List<Competition> activeResults = adapter.findByStatus(CompetitionStatus.ACTIVE);

        assertThat(activeResults).extracting(Competition::getName).containsExactly("Active Liga");
    }

    @Test
    void shouldDetectExistingName() {
        adapter.save(Competition.createWithDefaults("Liga Unica", "Desc"));

        assertThat(adapter.existsByName("Liga Unica")).isTrue();
        assertThat(adapter.existsByName("Liga Inexistente")).isFalse();
    }
}
