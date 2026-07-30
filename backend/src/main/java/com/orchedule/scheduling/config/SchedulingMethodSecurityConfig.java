package com.orchedule.scheduling.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables @PreAuthorize on scheduling's application services. Kept as an
 * explicit, documented switch inside the scheduling module rather than
 * assuming a global security config elsewhere — schedule generation is
 * one of the highest-impact mutations in the system (it rewrites the
 * public calendar) and deserves an unambiguous authorization boundary.
 */
@Configuration
@EnableMethodSecurity
public class SchedulingMethodSecurityConfig {
}
