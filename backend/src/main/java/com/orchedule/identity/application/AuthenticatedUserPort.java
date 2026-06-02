package com.orchedule.identity.application;

import java.util.Optional;
import java.util.UUID;

public interface AuthenticatedUserPort {

    Optional<AuthenticatedUserData> findByEmail(String email);

    Optional<AuthenticatedUserData> findActiveById(UUID id);

}