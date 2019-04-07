package com.vattima.lego.imaging.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PhotoMetaData {
    @JsonCreator
    public PhotoMetaData(@JsonProperty("path") Path path) {
        this.path = path;
    }

    private final Path path;
    private Map<String, String> keywords;
    private String md5;
    private String photoId;
    private int uploadReturnCode;
    private LocalDateTime uploadedTimeStamp;
    private boolean primary;

    @JsonIgnore
    public String getKeyword(String keyword) {
        return keywords.get(keyword);
    }

    @JsonIgnore
    public Path getFilename() {
        return path.getFileName();
    }

    @JsonIgnore
    public boolean canMove() {
        return (keywords.containsKey("uuid") && keywords.containsKey("bl"));
    }
}