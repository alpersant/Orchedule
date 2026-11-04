package com.orchedule.scheduling.planning.partial;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.orchedule.scheduling.application.port.SeasonContextPort;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planning/seasons/{seasonId}/games/{gameId}/pending")
public class PendingGameApi {
    private final NamedParameterJdbcTemplate db;
    private final PartialPlanningRepository repo;
    private final SeasonContextPort seasons;
    public PendingGameApi(NamedParameterJdbcTemplate db,PartialPlanningRepository repo,SeasonContextPort seasons){
        this.db=db;this.repo=repo;this.seasons=seasons;
    }
    public record PendingRequest(@NotNull Boolean pending,@Size(max=500) String reason){}
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public void update(@PathVariable UUID seasonId,@PathVariable UUID gameId,@Valid @RequestBody PendingRequest body){
        repo.lock(seasonId);seasons.assertSeasonIsSchedulable(seasonId);
        var rows=db.queryForList("""
            SELECT g.*,r.round_number,r.status AS round_status
            FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id
            WHERE g.id=:g AND r.season_id=:s FOR UPDATE OF g,r
            """,Map.of("g",gameId,"s",seasonId));
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra el partido en esta temporada");
        var game=rows.getFirst();
        if("CONFIRMED".equals(game.get("confirmation_status"))||"CONFIRMED".equals(game.get("round_status")))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Un partido confirmado no puede dejarse pendiente");
        if("CANCELLED".equals(game.get("round_status")))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Prepara de nuevo la jornada cancelada antes de cambiar sus partidos");
        if(body.pending()&&(game.get("field_id")!=null||game.get("scheduled_at")!=null))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Esta acción solo aplaza partidos sin horario; no libera reservas existentes");
        var parameters=new MapSqlParameterSource().addValue("g",gameId).addValue("p",body.pending())
            .addValue("reason",body.pending()?body.reason():null);
        db.update("UPDATE fixture_game SET deferred=:p,deferred_reason=:reason WHERE id=:g",parameters);
        db.update("UPDATE fixture_round SET version=version+1 WHERE id=:r",Map.of("r",game.get("round_id")));
        var actor=SecurityContextHolder.getContext().getAuthentication().getName();
        db.update("""
            INSERT INTO fixture_audit(id,season_id,fixture_game_id,action,operation,result,actor,detail)
            VALUES(:id,:s,:g,:a,:operation,'SUCCESS',:actor,:detail)
            """,new MapSqlParameterSource().addValue("id",UUID.randomUUID()).addValue("s",seasonId)
            .addValue("g",gameId).addValue("a",body.pending()?"DEFER_GAME":"RESUME_GAME")
            .addValue("operation",body.pending()?"Partido dejado pendiente":"Partido disponible para resolver")
            .addValue("actor",actor).addValue("detail",body.pending()?body.reason():"Se incluirá en la próxima resolución de pendientes"));
    }
}
