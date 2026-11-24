package com.orchedule.identity.domain;

public interface RefreshTokenHasher {
    String hash(String raw);
}