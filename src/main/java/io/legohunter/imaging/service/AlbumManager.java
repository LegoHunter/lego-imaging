package io.legohunter.imaging.service;

import io.legohunter.imaging.model.AlbumManifest;
import io.legohunter.imaging.model.PhotoMetaData;

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
