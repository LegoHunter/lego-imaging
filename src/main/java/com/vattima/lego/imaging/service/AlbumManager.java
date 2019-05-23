package com.vattima.lego.imaging.service;

import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.PhotoMetaData;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public interface AlbumManager {
    Optional<AlbumManifest> addPhoto(PhotoMetaData photoMetaData);

    Optional<AlbumManifest> movePhoto(PhotoMetaData photoMetaData);

    AlbumManifest getAlbumManifest(String uuid, String blItemNumber);

    AlbumManifest readAlbumManifest(Path path);
    void writeAlbumManifest(AlbumManifest albumManifest);

    Stream<AlbumManifest> findManifests(Path path);
}
