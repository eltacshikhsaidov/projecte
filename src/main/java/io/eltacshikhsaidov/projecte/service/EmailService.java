package io.eltacshikhsaidov.projecte.service;

public interface EmailService {
    void sendEmail(String from, String to, String subject, String message);
}
