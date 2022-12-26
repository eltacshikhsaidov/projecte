package io.eltacshikhsaidov.projecte.service;

import io.eltacshikhsaidov.projecte.enums.UserRole;
import io.eltacshikhsaidov.projecte.request.ReqUserRegistration;
import io.eltacshikhsaidov.projecte.response.RespStatus;
import io.eltacshikhsaidov.projecte.response.RespStatusList;

public interface RegistrationService {

    RespStatusList register(ReqUserRegistration reqUserRegistration, UserRole userRole);

    RespStatusList confirmToken(String token);
}
