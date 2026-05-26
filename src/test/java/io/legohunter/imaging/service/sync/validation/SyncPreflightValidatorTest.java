package io.legohunter.imaging.service.sync.validation;

import io.legohunter.imaging.model.AlbumManifest;
import io.legohunter.imaging.model.HostedAlbumMembershipRequest;
import io.legohunter.imaging.model.PhotoMetaDataV1;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SyncPreflightValidatorTest {
    private final SyncPreflightValidator validator = new SyncPreflightValidator();

    @Test
    void validateAlbumManifest_requiresAlbum() {
        SyncPreflightResult result = validator.validateAlbumManifest(null);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .extracting(SyncPreflightIssue::getType)
                .containsExactly(SyncPreflightIssueType.ALBUM_REQUIRED);
    }

    @Test
    void validateAlbumManifest_requiresPhotos() {
        AlbumManifest albumManifest = new AlbumManifest();

        SyncPreflightResult result = validator.validateAlbumManifest(albumManifest);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .extracting(SyncPreflightIssue::getType)
                .containsExactly(SyncPreflightIssueType.ALBUM_HAS_NO_PHOTOS);
    }

    @Test
    void validateAlbumManifest_detectsMissingPrimaryAndDuplicatePhotoIds(@TempDir Path tempDir) throws Exception {
        PhotoMetaDataV1 first = photo(tempDir, "front.jpg", "photo-1", false);
        PhotoMetaDataV1 second = photo(tempDir, "back.jpg", "photo-1", false);
        AlbumManifest albumManifest = new AlbumManifest();
        albumManifest.setPhotos(List.of(first, second));

        SyncPreflightResult result = validator.validateAlbumManifest(albumManifest);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .extracting(SyncPreflightIssue::getType)
                .contains(
                        SyncPreflightIssueType.PRIMARY_PHOTO_MISSING,
                        SyncPreflightIssueType.DUPLICATE_PHOTO_ID
                );
    }

    @Test
    void validatePhotoForReplace_requiresExistingFileAndPhotoId(@TempDir Path tempDir) {
        PhotoMetaDataV1 photoMetaData = new PhotoMetaDataV1(tempDir.resolve("missing.jpg"));

        SyncPreflightResult result = validator.validatePhotoForReplace(photoMetaData);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .extracting(SyncPreflightIssue::getType)
                .contains(
                        SyncPreflightIssueType.PHOTO_ID_MISSING,
                        SyncPreflightIssueType.PHOTO_FILE_MISSING
                );
    }

    @Test
    void validateAlbumMembership_requiresPrimaryPhotoInMembership() {
        HostedAlbumMembershipRequest request = HostedAlbumMembershipRequest.builder()
                .albumId("album-1")
                .primaryPhotoId("photo-3")
                .photoIds(new LinkedHashSet<>(List.of("photo-1", "photo-2")))
                .build();

        SyncPreflightResult result = validator.validateAlbumMembership(request);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .extracting(SyncPreflightIssue::getType)
                .containsExactly(SyncPreflightIssueType.PRIMARY_PHOTO_NOT_IN_MEMBERSHIP);
    }

    @Test
    void validateAlbumMembership_acceptsValidMembership() {
        HostedAlbumMembershipRequest request = HostedAlbumMembershipRequest.builder()
                .albumId("album-1")
                .primaryPhotoId("photo-1")
                .photoIds(new LinkedHashSet<>(List.of("photo-1", "photo-2")))
                .build();

        SyncPreflightResult result = validator.validateAlbumMembership(request);

        assertThat(result.isOk()).isTrue();
    }

    private PhotoMetaDataV1 photo(Path tempDir, String filename, String photoId, boolean primary) throws Exception {
        Path path = tempDir.resolve(filename);
        Files.write(path, new byte[]{1, 2, 3});
        PhotoMetaDataV1 photoMetaData = new PhotoMetaDataV1(path);
        photoMetaData.setPhotoId(photoId);
        photoMetaData.setPrimary(primary);
        return photoMetaData;
    }
}
