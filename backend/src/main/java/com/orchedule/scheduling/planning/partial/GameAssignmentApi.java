package com.orchedule.scheduling.planning.partial;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/planning/seasons/{seasonId}/games/{gameId}/assignment")
public class GameAssignmentApi {
    private final GameAssignmentService service;
    public GameAssignmentApi(GameAssignmentService service){this.service=service;}
    public record Assignment(@Min(0) long version,@NotBlank @Size(max=40) String localStart,UUID fieldId){}
    public record Revision(@Min(0) long version){}
    @PutMapping @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assign(@PathVariable UUID seasonId,@PathVariable UUID gameId,@Valid @RequestBody Assignment body){
        service.assign(seasonId,gameId,body.version(),body.localStart(),body.fieldId());
    }
    @DeleteMapping @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@PathVariable UUID seasonId,@PathVariable UUID gameId,@RequestParam long version){service.clear(seasonId,gameId,version);}
    @GetMapping("/fields")
    public List<Map<String,Object>> fields(@PathVariable UUID seasonId,@PathVariable UUID gameId,@RequestParam long version,@RequestParam String localStart){
        return service.fields(seasonId,gameId,version,localStart);
    }
    @PostMapping("/confirm") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirm(@PathVariable UUID seasonId,@PathVariable UUID gameId,@Valid @RequestBody Revision body){service.confirm(seasonId,gameId,body.version());}
    @PostMapping("/unconfirm") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unconfirm(@PathVariable UUID seasonId,@PathVariable UUID gameId,@Valid @RequestBody Revision body){service.unconfirm(seasonId,gameId,body.version());}
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> error(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",Objects.toString(e.getReason(),"No se pudo guardar el partido")));}
}
