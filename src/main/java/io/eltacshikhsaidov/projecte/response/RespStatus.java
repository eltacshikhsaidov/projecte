package io.eltacshikhsaidov.projecte.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static io.eltacshikhsaidov.projecte.util.translator.Translator.translate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespStatus implements Serializable {

    private Integer statusCode;
    private String statusMessage;

    private static final Integer SUCCESS_CODE = 1;


    public static RespStatus success() {
        return new RespStatus(SUCCESS_CODE, translate("SUCCESS_MESSAGE"));
    }


}

