package io.eltacshikhsaidov.projecte.service;

import io.eltacshikhsaidov.projecte.request.ReqUserLogin;
import io.eltacshikhsaidov.projecte.response.RespStatusList;

public interface LoginService {
    RespStatusList login(ReqUserLogin request);

}
