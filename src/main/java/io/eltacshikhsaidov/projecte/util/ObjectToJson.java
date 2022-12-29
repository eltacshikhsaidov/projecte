package io.eltacshikhsaidov.projecte.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class ObjectToJson {
    @SneakyThrows
    public static <T> String objectToJson(T t) {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return objectWriter.writeValueAsString(t);
    }
}
