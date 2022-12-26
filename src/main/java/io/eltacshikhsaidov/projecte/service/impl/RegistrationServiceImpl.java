package io.eltacshikhsaidov.projecte.service.impl;

import io.eltacshikhsaidov.projecte.enums.UserRole;
import io.eltacshikhsaidov.projecte.exception.ExceptionCodes;
import io.eltacshikhsaidov.projecte.model.User;
import io.eltacshikhsaidov.projecte.model.token.ConfirmationToken;
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

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@PropertySource(value = "classpath:mail.properties")
@Service
public class RegistrationServiceImpl implements RegistrationService {

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
                            "Invalid request data"
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
                            "Email is not valid"
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
                            "User with this email is already exists"
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
                LocalDateTime.now().plusMinutes(15),
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
                emailUtil.getConfirmContent(
                        fullConfirmUrl,
                        request.firstName()
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
                            "Invalid request data"
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
                            "Token not found"
                    )
            );
            return response;
        }

        if (confirmationToken.getConfirmedAt() != null) {
            log.warn("Token is already confirmed");
            response.setStatus(
                    new RespStatus(
                            ExceptionCodes.TOKEN_IS_ALREADY_CONFIRMED,
                            "Token is already confirmed"
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
                            "Token is expired"
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
}
