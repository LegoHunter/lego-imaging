package com.vattima.lego.imaging.model.bitly;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BitlyError {
    private String message;
    private List<Error> errors;
    private String resource;
    private String description;

    @Data
    private static class Error {
        private String field;
        private String message;
        @JsonProperty("error_code")
        private String errorCode;
    }
}
