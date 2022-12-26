package io.eltacshikhsaidov.projecte.service.impl;

import io.eltacshikhsaidov.projecte.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@PropertySource(value = "classpath:mail.properties")
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${mail.message.encoding}")
    private String encoding;

    @Override
    @Async
    public void sendEmail(String from, String to, String subject, String message) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
                    mimeMessage,
                    encoding

            );

            mimeMessageHelper.setText(message, true);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setFrom(from);
            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            log.error("Failed to send email, response: {}", e.getMessage());
            throw new IllegalStateException("Failed to send email");
        }
    }
}
