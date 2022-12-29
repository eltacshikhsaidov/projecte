package io.eltacshikhsaidov.projecte.service.impl;

import io.eltacshikhsaidov.projecte.exception.ExceptionCodes;
import io.eltacshikhsaidov.projecte.request.ReqUserLogin;
import io.eltacshikhsaidov.projecte.response.RespStatus;
import io.eltacshikhsaidov.projecte.response.RespStatusList;
import io.eltacshikhsaidov.projecte.service.LoginService;
import io.eltacshikhsaidov.projecte.util.AuthenticationUtil;
import io.eltacshikhsaidov.projecte.util.validator.EmailValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

import java.util.Random;

import static io.eltacshikhsaidov.projecte.util.translator.Translator.translate;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoginServiceImpl implements LoginService {

    private final EmailValidator emailValidator;
    private final AuthenticationUtil authenticationUtil;
    private final HttpServletRequest httpServletRequest;

    @Override
    public RespStatusList login(ReqUserLogin request) {
        log.info("login() service started with request: {}", request.email());
        RespStatusList response = new RespStatusList();

        if (request.email() == null || request.password() == null) {
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

        try {
            boolean isAuthenticated = authenticationUtil.isAuthenticated(request, httpServletRequest);

            if (isAuthenticated) {
                log.info("login() response: success");
                response.setStatus(RespStatus.success());
            } else {
                log.error("login failed");
                response.setStatus(
                        new RespStatus(
                                ExceptionCodes.LOGIN_FAILED,
                                translate("LOGIN_FAILED")
                        )
                );
            }

        } catch (Exception e) {
            log.error("exception message: {}", e.getMessage());
            if (e instanceof BadCredentialsException) {
                response.setStatus(
                        new RespStatus(
                                ExceptionCodes.BAD_CREDENTIALS,
                                translate("BAD_CREDENTIALS")
                        )
                );
            } else if (e instanceof DisabledException) {
                response.setStatus(
                        new RespStatus(
                                ExceptionCodes.EMAIL_IS_NOT_VERIFIED,
                                translate("EMAIL_IS_NOT_VERIFIED")
                        )
                );
            } else if (e instanceof LockedException) {
                response.setStatus(
                        new RespStatus(
                                ExceptionCodes.USER_LOCKED_BY_ADMIN,
                                translate("USER_LOCKED_BY_ADMIN")
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
