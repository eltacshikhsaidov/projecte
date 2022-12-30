package io.eltacshikhsaidov.projecte.service;

import io.eltacshikhsaidov.projecte.model.token.ConfirmationToken;

import java.util.Optional;

public interface ConfirmationTokenService {
    void saveConfirmationToken(ConfirmationToken token);

    Optional<ConfirmationToken> getToken(String token);

    int setConfirmedAt(String token);

    int setUpdatedAt(String oldToken, String newToken, Integer expiresAt);
}
