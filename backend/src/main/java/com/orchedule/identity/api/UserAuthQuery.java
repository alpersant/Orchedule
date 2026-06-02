package com.orchedule.identity.api;

import java.util.Optional;
import java.util.UUID;

public interface UserAuthQuery {

    Optional<UserAuthView> findByEmail(String email);

    Optional<UserAuthView> findActiveById(UUID id);
}