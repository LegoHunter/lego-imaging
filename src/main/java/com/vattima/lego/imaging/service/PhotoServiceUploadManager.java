package com.vattima.lego.imaging.service;

import java.nio.file.Path;

public interface PhotoServiceUploadManager {
    void queue(Path albumManifestPath);
    void updateAll();
}
