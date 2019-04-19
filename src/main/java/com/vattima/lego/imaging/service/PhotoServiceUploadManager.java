package com.vattima.lego.imaging.service;

import com.vattima.lego.imaging.model.AlbumManifest;

import java.nio.file.Path;

public interface PhotoServiceUploadManager {
    void queue(AlbumManifest albumManifest);
    void updateAll();
}
