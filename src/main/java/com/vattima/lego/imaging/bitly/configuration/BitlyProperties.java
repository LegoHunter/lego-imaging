package com.vattima.lego.imaging.bitly.configuration;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vattima.lego.imaging.LegoImagingException;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Setter
@Getter
@ToString
@Configuration
@ConfigurationProperties(prefix = "bitly")
public class BitlyProperties {
    private Path clientConfigDir;
    private Path clientConfigFile;
    private Bitly bitly;

    public BitlyProperties() {
        System.out.println("");
    }

    public void setClientConfigDir(Path clientConfigDir) {
        this.clientConfigDir = clientConfigDir;
        System.out.println(getClientConfigDir());
        loadPropertiesFromJson();
    }

    public void setClientConfigFile(Path clientConfigFile) {
        this.clientConfigFile = clientConfigFile;
        System.out.println(getClientConfigFile());
        loadPropertiesFromJson();
    }

    private void loadPropertiesFromJson() {
        Optional<Path> optionalDir = Optional.ofNullable(getClientConfigDir());
        Optional<Path> optionalFile = Optional.ofNullable(getClientConfigFile());
        if ((optionalDir.isPresent()) && (optionalFile.isPresent())) {
            Path jsonConfigFile = Paths.get(clientConfigDir.toString(), clientConfigFile.toString());
            if (Files.exists(jsonConfigFile)) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
                mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
                try {
                    bitly = mapper.readValue(jsonConfigFile.toFile(), Bitly.class);
                } catch (IOException e) {
                    throw new LegoImagingException(e);
                }
            } else {
                throw new LegoImagingException("[" + jsonConfigFile.toAbsolutePath() + "] does not exist");
            }
        }
    }

    @Data
    @JsonRootName(value = "bitly")
    public static class Bitly {
        private String accessToken;
    }
}
