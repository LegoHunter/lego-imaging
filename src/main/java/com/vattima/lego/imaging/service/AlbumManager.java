package com.vattima.lego.imaging.service;

import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.PhotoMetaData;

import java.nio.file.Path;
import java.util.Optional;

public interface AlbumManager {
    Optional<AlbumManifest> addPhoto(PhotoMetaData photoMetaData);

    Optional<AlbumManifest> movePhoto(PhotoMetaData photoMetaData);

    void updatePhotoService();

    Optional<AlbumManifest> getAlbumManifest(String uuid);

    AlbumManifest readAlbumManifest(Path path);
    void writeAlbumManifest(AlbumManifest albumManifest);
}
