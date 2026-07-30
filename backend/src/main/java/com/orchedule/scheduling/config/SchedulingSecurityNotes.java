package com.orchedule.scheduling.config;

/**
 * IMPORTANT: this module's services use @PreAuthorize
 * (ConfirmRoundService, CancelRoundService, RegenerateRoundService,
 * GenerateScheduleForSeasonService, GetScheduleService).
 *
 * @EnableMethodSecurity must be declared EXACTLY ONCE for the whole
 * application — normally on the main @SpringBootApplication class or a
 * single top-level SecurityConfig. A previous version of this module
 * declared its own @Configuration @EnableMethodSecurity class, which
 * risks a duplicate/conflicting method-security interceptor bean if the
 * root application also enables it (a common integration bug). That
 * module-local config class was removed for this reason.
 *
 * Action required: verify @EnableMethodSecurity exists once at the
 * application root before deploying this module.
 */
public final class SchedulingSecurityNotes {
    private SchedulingSecurityNotes() {}
}
