package io.legohunter.imaging.service.album.api;

import io.legohunter.imaging.model.AlbumManifest;
import io.legohunter.imaging.model.PhotoMetaDataV1;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public interface AlbumManager {
    Optional<AlbumManifest> addPhoto(PhotoMetaDataV1 photoMetaData);

    Optional<AlbumManifest> movePhoto(PhotoMetaDataV1 photoMetaData);

    AlbumManifest getAlbumManifest(String uuid, String blItemNumber);

    AlbumManifest readAlbumManifest(Path path);
    void writeAlbumManifest(AlbumManifest albumManifest);

    Stream<AlbumManifest> findManifests(Path path);
}
