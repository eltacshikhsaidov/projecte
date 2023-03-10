package io.eltacshikhsaidov.projecte.service.impl;

import io.eltacshikhsaidov.projecte.enums.UserRole;
import io.eltacshikhsaidov.projecte.exception.ExceptionCodes;
import io.eltacshikhsaidov.projecte.model.User;
import io.eltacshikhsaidov.projecte.model.token.ConfirmationToken;
import io.eltacshikhsaidov.projecte.repository.ConfirmationTokenRepository;
import io.eltacshikhsaidov.projecte.repository.UserRepository;
import io.eltacshikhsaidov.projecte.request.ReqUserRegistration;
import io.eltacshikhsaidov.projecte.response.RespStatus;
import io.eltacshikhsaidov.projecte.response.RespStatusList;
import io.eltacshikhsaidov.projecte.service.ConfirmationTokenService;
import io.eltacshikhsaidov.projecte.service.EmailService;
import io.eltacshikhsaidov.projecte.service.RegistrationService;
import io.eltacshikhsaidov.projecte.service.UserService;
import io.eltacshikhsaidov.projecte.util.EmailUtil;
import io.eltacshikhsaidov.projecte.util.validator.EmailValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.eltacshikhsaidov.projecte.util.translator.Translator.translate;
import static java.util.Objects.isNull;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@PropertySource(value = "classpath:mail.properties")
@Service
public class RegistrationServiceImpl implements RegistrationService {
    private final ConfirmationTokenRepository confirmationTokenRepository;

    private final UserService userService;
    private final UserRepository userRepository;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;
    private final EmailValidator emailValidator;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailUtil emailUtil;

    @Value("${mail.message.from}")
    private String from;

    @Value("${mail.message.confirm.subject}")
    private String confirmSubject;

    @Value("${mail.message.confirm.url}")
    private String confirmUrl;

    @Value("#{new Integer('${mail.confirm.expire.minute}')}")
    private Integer expiredMinutes;

    @Value("#{new Integer('${mail.send.limit.count}')}")
    private Integer mailSendLimitCount;

    @Override
    public RespStatusList register(ReqUserRegistration request, UserRole userRole) {
        log.info(
                "Starting function register() with request: {} and userRole {}",
                request,
                userRole
        );
        RespStatusList response = new RespStatusList();

        if (request.email() == null
                || request.firstName() == null
                || request.lastName() == null
                || request.password() == null) {
            log.warn("Invalid request data");
            response.setStatus(
                    new RespStatus(
                            ExceptionCodes.INVALID_REQUEST_DATA,
                            translate("INVALID_REQUEST_DATA")
                    )
            );
            return response;
        }

        boolean isValidEmail = emailValidator.test(request.email());

        if (!isValidEmail) {
            log.warn("Email is not valid");
            response.setStatus(
                    new RespStatus(
                            ExceptionCodes.EMAIL_IS_NOT_VALID,
                            translate("EMAIL_IS_NOT_VALID")
                    )
            );
            return response;
        }

        boolean isUserExists = userRepository.findByEmail(request.email()).isPresent();

        if (isUserExists) {
            log.warn("User with this email is already exists");
            response.setStatus(
                    new RespStatus(
                            ExceptionCodes.USER_WITH_THIS_EMAIL_IS_EXISTS,
                            translate("USER_WITH_THIS_EMAIL_IS_EXISTS")
                    )
            );
            return response;
        }

        String encodedPassword = bCryptPasswordEncoder.encode(request.password());

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(encodedPassword);
        user.setUserRole(userRole);

        log.info("Saving user to db started");
        userRepository.save(user);
        log.info("User saved successfully");

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(expiredMinutes),
                user
        );

        log.info("Saving generated token to db started");
        confirmationTokenService.saveConfirmationToken(confirmationToken);
        log.info("Token successfully saved to db");

        log.info("Starting sending email to {}", request.email());
        String fullConfirmUrl = confirmUrl + token;
        emailService.sendEmail(
                from,
                request.email(),
                confirmSubject,
                emailUtil.getEmailContent(
                        translate("EMAIL_CONFIRM_TITLE"),
                        translate("EMAIL_CONFIRM_HEADER"),
                        translate("EMAIL_CONFIRM_BUTTON_TEXT"),
                        translate("EMAIL_CONFIRM_RED_NOTE")
                                .replace("[expire]", expiredMinutes.toString()),
                        translate("EMAIL_CONFIRM_CONTENT")
                                .replace("[firstName]", request.firstName()),
                        fullConfirmUrl,
                        translate("EMAIL_CONFIRM_COUNTRY_STATE_FOOTER"),
                        "email.ftlh"
                )
        );
        log.info("Email successfully sent!");


        log.info("function register() response: success");
        response.setStatus(RespStatus.success());
        return response;
    }

    @Transactional
    @Override
    public RespStatusList confirmToken(String token) {
        log.info("confirmToken() function request: {}", token);
        RespStatusList response = new RespStatusList();

        if (token == null) {
            log.info("Invalid request data");
            response.setStatus(
                    new RespStatus(
                            ExceptionCodes.INVALID_REQUEST_DATA,
                            translate("INVALID_REQUEST_DATA")
                    )
            );
            return response;
        }

        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token)
                .orElse(null);

        if (confirmationToken == null) {
            log.warn("Token not found");
            response.setStatus(
                    new RespStatus(
                            ExceptionCodes.TOKEN_NOT_FOUND,
                            translate("TOKEN_NOT_FOUND")
                    )
            );
            return response;
        }

        if (confirmationToken.getConfirmedAt() != null) {
            log.warn("Token is already confirmed");
            response.setStatus(
                    new RespStatus(
                            ExceptionCodes.TOKEN_IS_ALREADY_CONFIRMED,
                            translate("TOKEN_IS_ALREADY_CONFIRMED")
                    )
            );
            return response;
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
        if (expiredAt.isBefore(LocalDateTime.now())) {
            log.warn("Token is expired");
            response.setStatus(
                    new RespStatus(
                            ExceptionCodes.TOKEN_IS_EXPIRED,
                            translate("TOKEN_IS_EXPIRED")
                    )
            );
            return response;
        }

        int setConfirmedAt = confirmationTokenService.setConfirmedAt(token);
        log.info("confirmedAt response: {}", setConfirmedAt);

        log.info("Enabling user started");
        int enableUser = userService.enableAppUser(confirmationToken.getUser().getEmail());
        log.info("enableAppUser response: {}", enableUser);

        log.info("confirmToken() function response: success");
        response.setStatus(RespStatus.success());
        return response;
    }

    @Override
    public RespStatusList updateToken(ReqUserRegistration reqUserRegistration) {

        String email = reqUserRegistration.email();
        log.info("reSendToken() started with request: {}", email);

        RespStatusList response = new RespStatusList();

        if (isNull(email)) {
            log.warn("Invalid request data");
            response.setStatus(
                    new RespStatus(
                            ExceptionCodes.INVALID_REQUEST_DATA,
                            translate("INVALID_REQUEST_DATA")
                    )
            );
            return response;
        }

        boolean isValidEmail = emailValidator.test(email);
        if (!isValidEmail) {
            log.warn("Email is not valid");
            response.setStatus(
                    new RespStatus(
                            ExceptionCodes.EMAIL_IS_NOT_VALID,
                            translate("EMAIL_IS_NOT_VALID")
                    )
            );
            return response;
        }

        log.info("Getting user info from db");
        User user = userRepository.findByEmail(email).orElse(null);

        if (!isNull(user)) {
            if (user.isEnabled()) {
                log.info("This email is already confirmed");
                response.setStatus(
                        new RespStatus(
                                ExceptionCodes.EMAIL_IS_ALREADY_CONFIRMED,
                                translate("EMAIL_IS_ALREADY_CONFIRMED")
                        )
                );

                return response;
            }
        }

        String oldToken = confirmationTokenRepository.findTokenByEmail(email).orElse(null);
        if (isNull(oldToken)) {
            log.warn("The specified email is not registered");
            // for security reasons we do not show the actual "not registered" message
            response.setStatus(
                    new RespStatus(
                            ExceptionCodes.NEW_TOKEN_WAS_SENT,
                            translate("NEW_TOKEN_WAS_SENT")
                    )
            );
            return response;
        }

        log.info("Checking if user exceeded limit count for sending email");
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(oldToken).orElse(null);

        if (!isNull(confirmationToken)) {
            if (confirmationToken.getCountRefreshToken() > mailSendLimitCount) {
                log.info("You exceeded email sending limit for last 2 minutes");
                response.setStatus(
                        new RespStatus(
                                ExceptionCodes.EXCEEDED_EMAIL_SENDING_LIMIT,
                                translate("EXCEEDED_EMAIL_SENDING_LIMIT")
                        )
                );
                return response;
            }
        }

        log.info("Creating new token");
        String newToken = UUID.randomUUID().toString();

        log.info("Updating oldToken started");
        int isUpdated = confirmationTokenService.setUpdatedAt(oldToken, newToken, expiredMinutes);
        log.info("oldToken updated response: {}", isUpdated == 1 ? "Success" : "Failure");

        if (!isNull(user)) {
            log.info("Starting sending email to {}", email);
            String fullConfirmUrl = confirmUrl + newToken;
            emailService.sendEmail(
                    from,
                    email,
                    confirmSubject,
                    emailUtil.getEmailContent(
                            translate("EMAIL_CONFIRM_TITLE"),
                            translate("EMAIL_CONFIRM_HEADER"),
                            translate("EMAIL_CONFIRM_BUTTON_TEXT"),
                            translate("EMAIL_CONFIRM_RED_NOTE")
                                    .replace("[expire]", expiredMinutes.toString()),
                            translate("EMAIL_CONFIRM_CONTENT")
                                    .replace("[firstName]", user.getFirstName()),
                            fullConfirmUrl,
                            translate("EMAIL_CONFIRM_COUNTRY_STATE_FOOTER"),
                            "email.ftlh"
                    )
            );
            log.info("Email successfully sent!");
        }


        response.setStatus(
                new RespStatus(
                        ExceptionCodes.NEW_TOKEN_WAS_SENT,
                        translate("NEW_TOKEN_WAS_SENT")
                )
        );
        return response;
    }


}
