package io.eltacshikhsaidov.projecte.job;

import io.eltacshikhsaidov.projecte.model.token.ConfirmationToken;
import io.eltacshikhsaidov.projecte.repository.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@EnableAsync
@PropertySource("classpath:mail.properties")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmailScheduler {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Value("#{new Integer('${mail.send.limit.count}')}")
    private Integer mailSendLimitCount;


    @Async
    @Scheduled(fixedDelay = 2 * 60 * 1000)
    public void resetEmailSendingLimit() {
        try {
            log.info("Getting all rows which is exceeded limit for sending email");
            List<ConfirmationToken> confirmationTokens =
                    confirmationTokenRepository.findAllByCountRefreshTokenGreaterThan(mailSendLimitCount);
            log.info("confirmationTokens: {}", confirmationTokens);

            confirmationTokens.forEach(
                    confirmationToken -> {
                        log.info(
                                "Reset email sending limit for confirmationTokenId: {} started",
                                confirmationToken.getId()
                        );
                        int reset = confirmationTokenRepository.resetLimitCount(confirmationToken.getId());
                        if (reset == 1) {
                            log.info("Reset completed successfully");
                        } else {
                            log.error("Failed to reset count");
                        }
                    }
            );

        } catch (Exception e) {
            log.error("Database error, errorMessage: {}", e.getMessage());
        }
    }
}
