package com.vattima.lego.imaging.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vattima.lego.imaging.LegoImagingException;
import lombok.Data;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Data
public class PhotoMetaData {
    @JsonCreator
    public PhotoMetaData(@JsonProperty("path") Path path) {
        this.path = path.getParent();
        this.filename = path.getFileName();
    }

    @JsonIgnore
    private final Path path;

    private Path filename;
    private Map<String, String> keywords;
    private String md5;
    private String photoId;
    private int uploadReturnCode;
    private LocalDateTime uploadedTimeStamp;
    private boolean primary;
    private boolean changed;

    @JsonIgnore
    public Path getAbsolutePath() {
        return path.resolve(getFilename());
    }

    @JsonIgnore
    public String getKeyword(String keyword) {
        return Optional.ofNullable(keywords)
                       .map(m -> m.get(keyword))
                       .orElse(null);
    }

    @JsonIgnore
    public boolean canMove() {
        return (keywords.containsKey("uuid") && keywords.containsKey("bl"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotoMetaData that = (PhotoMetaData) o;
        return Objects.equals(getFilename(), that.getFilename());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFilename());
    }
}