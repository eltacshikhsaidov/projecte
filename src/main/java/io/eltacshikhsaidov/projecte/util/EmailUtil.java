package io.eltacshikhsaidov.projecte.util;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmailUtil {

    private final Configuration configuration;

    public String getConfirmContent(String url, String firstName, Integer expiredMinute) {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("url", url);
        model.put("firstName", firstName);
        model.put("expiredMinute", expiredMinute);
        try {
            configuration.getTemplate("email.ftlh").process(model, stringWriter);
        } catch (TemplateException | IOException e) {
            throw new RuntimeException(e);
        }
        return stringWriter.getBuffer().toString();
    }
}
