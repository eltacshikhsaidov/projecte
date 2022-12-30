package io.eltacshikhsaidov.projecte.repository;

import io.eltacshikhsaidov.projecte.model.token.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    Optional<ConfirmationToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("UPDATE ConfirmationToken c SET c.confirmedAt = ?2 WHERE c.token = ?1")
    int updateConfirmedAt(String token, LocalDateTime confirmedAt);

    @Transactional
    @Modifying
    @Query(value = "UPDATE confirmation_token " +
            "SET token = ?2, " +
            "token_updated_at = current_timestamp," +
            "expires_at = current_timestamp + ?3 * interval '1 minute'," +
            "count_refresh_token = count_refresh_token + 1 " +
            "where token = ?1", nativeQuery = true)
    int updateToken(String oldToken, String newToken, Integer expireMinutes);

    @Query("SELECT c.token " +
            "from ConfirmationToken c " +
            "inner join User u " +
            "on u.id = c.user.id " +
            "where u.email = ?1")
    Optional<String> findTokenByEmail(String email);

    List<ConfirmationToken> findAllByCountRefreshTokenGreaterThan(Integer countRefreshToken);


    @Transactional
    @Modifying
    @Query("update ConfirmationToken c set c.countRefreshToken = 0 where c.id = ?1")
    int resetLimitCount(Long id);
}
