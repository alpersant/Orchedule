package com.orchedule.identity.application;

import java.util.Optional;
import java.util.UUID;

public interface UserQueryService {

    Optional<UserPrincipal> findActiveById(UUID id);

    record UserPrincipal(
            UUID id,
            String email,
            String fullName,
            String role
    ) {
    }
}