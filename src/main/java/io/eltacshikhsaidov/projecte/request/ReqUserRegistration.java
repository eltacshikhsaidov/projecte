package io.eltacshikhsaidov.projecte.request;
public record ReqUserRegistration(
        String firstName,
        String lastName,
        String email,
        String password
) {}
