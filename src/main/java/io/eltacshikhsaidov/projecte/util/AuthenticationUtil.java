package io.eltacshikhsaidov.projecte.util;

import io.eltacshikhsaidov.projecte.request.ReqUserLogin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationUtil {

    private final AuthenticationManager authenticationManager;

    public boolean isAuthenticated(ReqUserLogin request, HttpServletRequest httpServletRequest) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        HttpSession httpSession = httpServletRequest.getSession(true);
        httpSession.setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext);

        return securityContext.getAuthentication().isAuthenticated();
    }
}
