package com.orchedule.season.application;
import com.orchedule.season.domain.*;
import com.orchedule.season.api.dto.SeasonResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.util.*;

@Service
public class ExtendSeasonEndDateService {
    private final NamedParameterJdbcTemplate db;
    private final SeasonRepository repository;
    public ExtendSeasonEndDateService(NamedParameterJdbcTemplate db,SeasonRepository repository){this.db=db;this.repository=repository;}
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public SeasonResponse extend(UUID id,LocalDate expectedStart,LocalDate expectedEnd,LocalDate end){
        db.getJdbcTemplate().execute("SELECT pg_advisory_xact_lock(813051031)");
        if(db.queryForList("SELECT id FROM season WHERE id=:id FOR UPDATE",Map.of("id",id)).isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la temporada");
        var current=repository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la temporada"));
        if(!current.startDate().equals(expectedStart)||!current.endDate().equals(expectedEnd))throw new ResponseStatusException(HttpStatus.CONFLICT,"Las fechas han cambiado en otra sesión. Vuelve a comprobar la temporada.");
        if(!current.active()||current.status()!=SeasonStatus.ACTIVE)throw new ResponseStatusException(HttpStatus.CONFLICT,"Solo se pueden ampliar desde este asistente las temporadas activas");
        if(end==null||!end.isAfter(current.endDate())||end.getYear()>9999)throw new IllegalArgumentException("Elige una fecha final posterior a la actual");
        SeasonValidator.validate(current.name(),current.startDate(),end,current.status());
        var updated=repository.update(id,current.name(),current.startDate(),end,current.status(),current.active());
        return new SeasonResponse(updated.id(),updated.competitionId(),updated.name(),updated.startDate(),updated.endDate(),updated.status(),updated.active());
    }
}
