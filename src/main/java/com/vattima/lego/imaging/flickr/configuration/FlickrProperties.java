package com.vattima.lego.imaging.flickr.configuration;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vattima.lego.imaging.LegoImagingException;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@ToString
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "flickr")
public class FlickrProperties {
    private Path clientConfigDir;
    private Path clientConfigFile;
    private String applicationName;
    private Boolean debugRequest;
    private Boolean debugStream;
    private String userId;
    private Flickr flickr;

    public void setClientConfigDir(Path clientConfigDir) {
        this.clientConfigDir = clientConfigDir;
        loadPropertiesFromJson();
    }

    public void setClientConfigFile(Path clientConfigFile) {
        this.clientConfigFile = clientConfigFile;
        loadPropertiesFromJson();
    }

    private void loadPropertiesFromJson() {
        Optional<Path> optionalDir = Optional.ofNullable(getClientConfigDir());
        Optional<Path> optionalFile = Optional.ofNullable(getClientConfigFile());
        if ((optionalDir.isPresent()) && (optionalFile.isPresent())) {
            Path jsonConfigFile = Path.of(clientConfigDir.toString(), clientConfigFile.toString());
            if (Files.exists(jsonConfigFile)) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
                mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
                try {
                    flickr = mapper.readValue(jsonConfigFile.toFile(), Flickr.class);
                    userId = flickr.getUserId();
                    log.info("Loaded secure configuration [{}] from path [{}]", clientConfigFile, clientConfigDir);
                } catch (IOException e) {
                    throw new LegoImagingException(e);
                }
            } else {
                throw new LegoImagingException("[" + jsonConfigFile.toAbsolutePath() + "] does not exist");
            }
        }
    }

    public Flickr getFlickr() {
        return Optional.ofNullable(flickr).orElseThrow(() -> new LegoImagingException("flickr properties have not been loaded"));
    }

    @Data
    @JsonRootName(value = "flickr")
    public static class Flickr {
        private String userId;
        private List<Application> applications = new ArrayList<>();

        public Application getApplication(String name) {
            return applications.stream().filter(a -> a.getName().equals(name)).findFirst().orElseThrow(() -> new LegoImagingException("["+name+" was not found"));
        }
    }

    @Data
    public static class Application {
        private String name;
        private Secrets secrets;
    }

    @Data
    public static class Secrets {
        private String key;
        private String secret;
        private String token;
        private String tokenSecret;
    }
}
