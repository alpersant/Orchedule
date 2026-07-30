package com.orchedule.team.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Published by the team module whenever a team's day/hour preferences
 * change. Other modules (scheduling) react to this asynchronously via
 * Spring Modulith's @ApplicationModuleListener rather than being called
 * synchronously from team's write path — this keeps team's services free
 * of any knowledge that scheduling exists.
 */
public record TeamPreferencesChangedEvent(UUID teamId, UUID seasonId, OffsetDateTime occurredAt) {

    public static TeamPreferencesChangedEvent of(UUID teamId, UUID seasonId) {
        return new TeamPreferencesChangedEvent(teamId, seasonId, OffsetDateTime.now());
    }
}
