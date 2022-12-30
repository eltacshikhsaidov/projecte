package io.eltacshikhsaidov.projecte.controller;

import io.eltacshikhsaidov.projecte.model.User;
import io.eltacshikhsaidov.projecte.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/private")
@RequiredArgsConstructor
public class PrivateController {


    private final UserRepository userRepository;

    @GetMapping(value = "/profile")
    public User test(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }
}
