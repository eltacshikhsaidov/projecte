package io.eltacshikhsaidov.projecte.response;

import lombok.Data;

@Data
public class RespUserRegistration {
    private String token;
    private RespStatus status;
}
