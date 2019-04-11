package com.vattima.lego.imaging.service;

import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.PhotoMetaData;

import java.util.Optional;

public interface AlbumManager {
    Optional<AlbumManifest> addPhoto(PhotoMetaData photoMetaData);

    Optional<AlbumManifest> movePhoto(PhotoMetaData photoMetaData);

    AlbumManifest uploadToPhotoService(PhotoMetaData photoMetaData, AlbumManifest albumManifest);

    Optional<AlbumManifest> getAlbumManifest(String uuid);
}
