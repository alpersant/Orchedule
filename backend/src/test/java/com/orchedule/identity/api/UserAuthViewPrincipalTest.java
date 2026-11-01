package com.orchedule.identity.api;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserAuthViewPrincipalTest {
    @Test
    void authenticationNameIsOnlyTheUserUuidAndFitsAuditColumn() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String hash = "test-only-hash-" + "x".repeat(60);
        var user = new UserAuthView(id, "organizer@example.test", "Organizador de prueba", "ORGANIZER", hash, true);
        var auth = new UsernamePasswordAuthenticationToken(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER")));
        assertEquals(id.toString(), auth.getName());
        assertEquals(36, auth.getName().length());
        assertTrue(auth.getName().length() <= 180);
        assertFalse(auth.getName().contains(hash));
        assertFalse(auth.getName().contains("passwordHash"));
    }

    @Test
    void principalAndAuthenticationDebugOutputDoNotContainTheHash() {
        String hash = "test-secret-hash-that-must-never-be-logged";
        var user = new UserAuthView(UUID.randomUUID(), "private@example.test", "Nombre privado", "ADMIN", hash, true);
        var auth = new UsernamePasswordAuthenticationToken(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertFalse(user.toString().contains(hash));
        assertFalse(user.toString().contains("passwordHash"));
        assertFalse(user.toString().contains(user.email()));
        assertFalse(user.toString().contains(user.fullName()));
        assertFalse(auth.toString().contains(hash));
        assertSame(user, auth.getPrincipal());
        assertTrue(auth.isAuthenticated());
        assertEquals("ROLE_ADMIN", auth.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void recordAccessorsUsedByExistingAuthenticationCodeRemainCompatible() {
        UUID id = UUID.randomUUID();
        var user = new UserAuthView(id, "login@example.test", "Nombre", "USER", "test-only-hash", true);
        assertEquals(id, user.id());
        assertEquals("login@example.test", user.email());
        assertEquals("test-only-hash", user.passwordHash());
        assertTrue(user.active());
    }

    @Test
    void missingIdentityFailsInsteadOfRecordingAnAmbiguousActor() {
        var user = new UserAuthView(null, "login@example.test", "Nombre", "USER", "test-only-hash", true);
        assertThrows(IllegalStateException.class, user::getName);
    }
}
