package com.vattima.lego.imaging.bitly.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vattima.lego.imaging.BitlyException;
import com.vattima.lego.imaging.api.bitly.BitlinksAPI;
import com.vattima.lego.imaging.model.bitly.BitlyError;
import com.vattima.lego.imaging.service.bitly.BitlinksService;
import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Optional;

@Configuration
public class BitlyConfiguration {
    private Feign.Builder builder;

    @Bean
    public BitlinksService bitlinksService(BitlinksAPI bitlinksAPI) {
        return new BitlinksService(bitlinksAPI);
    }

    @Bean
    @Qualifier("bitlyObjectMapper")
    public ObjectMapper bitlyObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new StdDateFormat());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Bean
    public BitlinksAPI bitlinksAPI(@Qualifier("bitlyObjectMapper") ObjectMapper bitlyObjectMapper, BitlyProperties bitlyProperties) {
        return builder(bitlyObjectMapper, bitlyProperties)
                .target(BitlinksAPI.class, bitlyProperties.getBitly().getBaseUrl());
    }

    private Feign.Builder builder(@Qualifier("bitlyObjectMapper") ObjectMapper bitlyObjectMapper, BitlyProperties bitlyProperties) {
        if (null == builder) {
            builder = Feign
                    .builder()
                    .client(new OkHttpClient())
                    .encoder(new JacksonEncoder(bitlyObjectMapper))
                    .decoder(new JacksonDecoder(bitlyObjectMapper))
                    .errorDecoder(new BitlyErrorDecoder(bitlyObjectMapper))
                    .requestInterceptor(new OAuthRequestInterceptor(bitlyProperties.getBitly().getAccessToken()))
                    .logger(new Slf4jLogger(BitlinksAPI.class))
                    .logLevel(feign.Logger.Level.FULL);

        }
        return builder;
    }

    private static class OAuthRequestInterceptor implements RequestInterceptor {
        private String headerValue;

        public OAuthRequestInterceptor(String accessToken) {
            this.headerValue = "Bearer %s".formatted(accessToken);
        }

        @Override
        public void apply(RequestTemplate requestTemplate) {
            requestTemplate.header("Authorization", this.headerValue);
        }
    }


    @RequiredArgsConstructor
    @Slf4j
    private static class BitlyErrorDecoder implements ErrorDecoder {
        private final ObjectMapper bitlyObjectMapper;

        @Override
        public Exception decode(String methodKey, Response response) {
            final BitlyError bitlyError =
                    Optional.ofNullable(response.body())
                            .map(b -> {
                                try {
                                    return bitlyObjectMapper.readValue(b.asInputStream(), BitlyError.class);
                                } catch (IOException e) {
                                    BitlyError error = new BitlyError();
                                    return error;
                                }
                            })
                            .get();
            log.error("BitlyError Error [{}]", bitlyError);
            return new BitlyException(bitlyError, "%d - %s".formatted(response.status(), response.reason()));
        }
    }
}
