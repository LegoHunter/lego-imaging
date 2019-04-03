package com.vattima.lego.imaging.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vattima.lego.imaging.LegoImagingException;
import lombok.Data;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@JsonInclude(Include.NON_NULL)
public class AlbumManifest {
    private String photosetId;
    private String title;
    private String description;
    private URL url;
    private String uuid;
    private String blItemNumber;
    private List<PhotoMetaData> photos = new ArrayList<>();
    private boolean isNew;

    @JsonIgnore
    private Path path;

    @JsonIgnore
    public Optional<PhotoMetaData> getPhotoByFilename(Path filename) {
        return photos.stream()
                     .filter(p -> p.getPath().getFileName().equals(filename))
                     .reduce((a, b) -> null);
    }

    @JsonIgnore
    public PhotoMetaData getPrimaryPhoto() {
        Optional<PhotoMetaData> primary = photos.stream()
                                                .filter(PhotoMetaData::isPrimary)
                                                .reduce((a, b) -> null);
        return primary.orElseGet(() -> {
            if (photos.size() > 0) {
                return photos.get(0);
            } else {
                throw new LegoImagingException("No photos exist from which to select a primary photo");
            }
        });
    }

    public static AlbumManifest fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        try {
            return mapper.readValue(json, AlbumManifest.class);
        } catch (IOException e) {
            throw new LegoImagingException(e);
        }
    }

    public static AlbumManifest fromJson(Path jsonFile) {
        if (Files.exists(jsonFile)) {
            try {
                return fromJson(new String(Files.readAllBytes(jsonFile)));
            } catch (IOException e) {
                throw new LegoImagingException(e);
            }
        } else {
            AlbumManifest albumManifest = new AlbumManifest();
            albumManifest.setNew(true);
            return albumManifest;
        }
    }

    public static AlbumManifest toJson(Path jsonFile, AlbumManifest albumManifest) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        try {
            mapper.writeValue(Files.newBufferedWriter(jsonFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE), albumManifest);
        } catch (IOException e) {
            throw new LegoImagingException(e);
        }
        return albumManifest;
    }
}
