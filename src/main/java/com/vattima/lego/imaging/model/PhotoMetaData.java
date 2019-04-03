package com.vattima.lego.imaging.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Data
public class PhotoMetaData {
    private Path path;
    private String md5;
    private String photoId;
    private int uploadReturnCode;
    private LocalDateTime uploadedTimeStamp;
    private boolean primary;
}