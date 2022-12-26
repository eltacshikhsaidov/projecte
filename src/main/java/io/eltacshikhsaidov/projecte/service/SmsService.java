package io.eltacshikhsaidov.projecte.service;

public interface SmsService {
    void sendSms(String from, String to, String message);
}
