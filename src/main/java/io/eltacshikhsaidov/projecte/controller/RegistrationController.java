package io.eltacshikhsaidov.projecte.controller;

import io.eltacshikhsaidov.projecte.enums.UserRole;
import io.eltacshikhsaidov.projecte.request.ReqUserRegistration;
import io.eltacshikhsaidov.projecte.response.RespStatusList;
import io.eltacshikhsaidov.projecte.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/register")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RegistrationController {

    private final RegistrationService registrationService;

    @GetMapping
    public RespStatusList register(@RequestBody ReqUserRegistration request) {
        return registrationService.register(request, UserRole.USER);
    }

    @GetMapping(value = "/confirm")
    public RespStatusList confirm(@RequestParam(name = "token") String token) {
        return registrationService.confirmToken(token);
    }

}
