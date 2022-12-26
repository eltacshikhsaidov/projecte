package io.eltacshikhsaidov.projecte.util.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@PropertySource(value = "classpath:mail.properties")
public class EmailValidator implements Predicate<String> {

    @Value("${mail.validate.regex.regexp}")
    public String regex;

    @Override
    public boolean test(String email) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
