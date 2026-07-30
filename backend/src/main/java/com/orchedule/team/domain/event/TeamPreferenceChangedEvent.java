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
public record TeamPreferenceChangedEvent(UUID teamId, UUID seasonId, OffsetDateTime occurredAt) {

    public static TeamPreferenceChangedEvent of(UUID teamId, UUID seasonId) {
        return new TeamPreferenceChangedEvent(teamId, seasonId, OffsetDateTime.now());
    }
}
