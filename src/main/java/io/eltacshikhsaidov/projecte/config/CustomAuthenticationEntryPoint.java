package io.eltacshikhsaidov.projecte.config;

import io.eltacshikhsaidov.projecte.exception.ExceptionCodes;
import io.eltacshikhsaidov.projecte.response.RespStatus;
import io.eltacshikhsaidov.projecte.response.RespStatusList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import static io.eltacshikhsaidov.projecte.util.ObjectToJson.objectToJson;
import static io.eltacshikhsaidov.projecte.util.translator.Translator.translate;

@Configuration
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        RespStatusList resp = new RespStatusList();
        resp.setStatus(
                new RespStatus(
                        ExceptionCodes.ACCESS_DENIED,
                        translate("ACCESS_DENIED")
                )
        );

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(403);
        response.getWriter().write(objectToJson(resp));
    }
}
