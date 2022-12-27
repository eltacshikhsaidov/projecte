package io.eltacshikhsaidov.projecte.service.impl;

import io.eltacshikhsaidov.projecte.exception.ExceptionCodes;
import io.eltacshikhsaidov.projecte.request.ReqUserLogin;
import io.eltacshikhsaidov.projecte.response.RespStatus;
import io.eltacshikhsaidov.projecte.response.RespStatusList;
import io.eltacshikhsaidov.projecte.service.LoginService;
import io.eltacshikhsaidov.projecte.util.AuthenticationUtil;
import io.eltacshikhsaidov.projecte.util.validator.EmailValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoginServiceImpl implements LoginService {

    private final EmailValidator emailValidator;
    private final AuthenticationUtil authenticationUtil;
    @Override
    public RespStatusList login(ReqUserLogin request) {
        log.info("login() service started with request: {}", request.email());
        RespStatusList response = new RespStatusList();

        if (request.email() == null || request.password() == null) {
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

        try {
            boolean isAuthenticated = authenticationUtil.isAuthenticated(request);

            if (isAuthenticated) {
                log.info("login() response: success");
                response.setStatus(RespStatus.success());
            } else {
                log.error("login failed");
                response.setStatus(
                        new RespStatus(
                                ExceptionCodes.LOGIN_FAILED,
                                "Login failed"
                        )
                );
            }

        } catch (Exception e) {
            log.error("exception message: {}", e.getMessage());
            if (e instanceof BadCredentialsException) {
                response.setStatus(
                        new RespStatus(
                                ExceptionCodes.BAD_CREDENTIALS,
                                "Bad credentials"
                        )
                );
            } else if (e instanceof DisabledException) {
                response.setStatus(
                        new RespStatus(
                                ExceptionCodes.EMAIL_IS_NOT_VERIFIED,
                                "Email is not verified"
                        )
                );
            } else if (e instanceof LockedException) {
                response.setStatus(
                        new RespStatus(
                                ExceptionCodes.USER_LOCKED_BY_ADMIN,
                                "Locked by admin"
                        )
                );
            } else {
                response.setStatus(
                        new RespStatus(
                                new Random().nextInt(),
                                e.getMessage()
                        )
                );
            }
        }

        return response;
    }
}
