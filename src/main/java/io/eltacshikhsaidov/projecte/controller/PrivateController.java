package io.eltacshikhsaidov.projecte.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/private")
@RequiredArgsConstructor
public class PrivateController {

    @GetMapping(value = "/test")
    public String test(Authentication authentication) {
        return "test" + authentication.getName();
    }
}
