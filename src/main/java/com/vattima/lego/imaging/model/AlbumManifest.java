package com.vattima.lego.imaging.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vattima.lego.imaging.LegoImagingException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dto.BricklinkInventory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Slf4j
public class AlbumManifest {
    private String photosetId;
    private String title;
    private String description;
    private URL url;
    private URL shortUrl;
    private String uuid;
    private String blItemNumber;
    private List<PhotoMetaData> photos = new ArrayList<>();
    private boolean isNew;

    @JsonIgnore
    public Optional<PhotoMetaData> getPhotoByFilename(Path filename) {
        return photos.stream()
                     .filter(p -> p.getFilename()
                                   .equals(filename))
                     .reduce((a, b) -> null);
    }

    @JsonIgnore
    public PhotoMetaData getPrimaryPhoto() {
         Optional<PhotoMetaData> primary = photos.stream()
                                                .filter(PhotoMetaData::getPrimary)
                                                .reduce((a, b) -> {
                                                    log.warn("Multiple Photos are marked primary for uuid [{}], item [{}] - choosing the first one below", getUuid(), getBlItemNumber());
                                                    log.warn("[{}]", a);
                                                    log.warn("[{}]", b);
                                                    return a;
                                                });
        return primary.orElseGet(() -> {
            if (photos.size() > 0) {
                return photos.get(0);
            } else {
                throw new LegoImagingException("No photos exist from which to select a primary photo");
            }
        });
    }

    @JsonIgnore
    public boolean hasPrimaryPhoto() {
        Optional<PhotoMetaData> primary = photos.stream()
                                                .filter(PhotoMetaData::getPrimary)
                                                .findFirst();
        return primary.isPresent();
    }

    @JsonIgnore
    public Path getAlbumManifestPath(Path root) {
        return getAlbumManifestPath(root, getUuid(), getBlItemNumber());
    }

    @JsonIgnore
    public Path getAlbumManifestFile(Path root) {
        return getAlbumManifestFile(root, getUuid(), getBlItemNumber());
    }

    @JsonIgnore
    public static Path getAlbumManifestPath(Path root, String uuid, String blItemNumber) {
        return root.resolve(blItemNumber + "-" + uuid);
    }

    @JsonIgnore
    public static Path getAlbumManifestFile(Path root, String uuid, String blItemNumber) {
        return getAlbumManifestPath(root, uuid, blItemNumber).resolve(blItemNumber + "-" + uuid + "-manifest.json");
    }

    @JsonIgnore
    public static Path getAlbumManifestPath(Path root, PhotoMetaData photoMetaData) {
        return getAlbumManifestPath(root, photoMetaData.getKeyword("uuid"), photoMetaData.getKeyword("bl"));
    }

    @JsonIgnore
    public static Path getAlbumManifestFile(Path root, PhotoMetaData photoMetaData) {
        return getAlbumManifestFile(root, photoMetaData.getKeyword("uuid"), photoMetaData.getKeyword("bl"));
    }

    @JsonIgnore
    public List<String> getPhotoIds() {
        return photos.stream().map(p -> p.getPhotoId()).collect(Collectors.toList());
    }

    @JsonIgnore
    public String[] getPhotoIdsArray() {
        List<String> photoIds = getPhotoIds();
        return photoIds.toArray(new String[]{});
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

    public String getTitle() {
        return Optional.ofNullable(title)
                       .orElseGet(() -> Optional.ofNullable(getBlItemNumber())
                                       .orElseThrow(() -> new LegoImagingException("No bricklink item number set")));
    }

    public String getDescription() {
        return Optional.ofNullable(description)
                       .orElseGet(() -> String.format("%s - Lot Id [%s]",
                               Optional.ofNullable(getBlItemNumber())
                                       .orElseThrow(() -> new LegoImagingException("No bricklink item number set")),
                               Optional.ofNullable(getUuid())
                                       .orElseThrow(() -> new LegoImagingException("No uuid set"))));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlbumManifest that = (AlbumManifest) o;
        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }

    public void updateFromBricklinkInventory(BricklinkInventory bricklinkInventory) {
        setTitle(String.format("%s - %s", bricklinkInventory.getBlItemNo(), bricklinkInventory.getItemName()));
        setDescription(bricklinkInventory.getUuid());
    }
}
