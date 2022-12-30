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

    public String getEmailContent(
            String title,
            String header,
            String buttonText,
            String redNote,
            String content,
            String url,
            String countryAndState,
            String templateName) {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("title", title);
        model.put("header", header);
        model.put("buttonText", buttonText);
        model.put("redNote", redNote);
        model.put("content", content);
        model.put("url", url);
        model.put("countryAndState", countryAndState);
        try {
            configuration.getTemplate(templateName).process(model, stringWriter);
        } catch (TemplateException | IOException e) {
            throw new RuntimeException(e);
        }
        return stringWriter.getBuffer().toString();
    }
}
