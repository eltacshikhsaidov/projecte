package io.eltacshikhsaidov.projecte.controller;

import io.eltacshikhsaidov.projecte.request.ReqUserLogin;
import io.eltacshikhsaidov.projecte.response.RespStatusList;
import io.eltacshikhsaidov.projecte.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/login")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoginController {

    private final LoginService loginService;

    @GetMapping
    public RespStatusList login(@RequestBody ReqUserLogin request) {
        return loginService.login(request);
    }
}
