package io.legohunter.imaging.service.sync.validation;

import io.legohunter.imaging.model.AlbumManifest;
import io.legohunter.imaging.model.HostedAlbumMembershipRequest;
import io.legohunter.imaging.model.PhotoMetaDataV1;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
public class SyncPreflightValidator {
    public SyncPreflightResult validateAlbumManifest(AlbumManifest albumManifest) {
        List<SyncPreflightIssue> issues = new ArrayList<>();
        if (albumManifest == null) {
            issues.add(error(SyncPreflightIssueType.ALBUM_REQUIRED, "Album manifest is required"));
            return new SyncPreflightResult(issues);
        }

        List<PhotoMetaDataV1> photos = Optional.ofNullable(albumManifest.getPhotos()).orElse(List.of());
        if (photos.isEmpty()) {
            issues.add(error(SyncPreflightIssueType.ALBUM_HAS_NO_PHOTOS, "Album manifest must contain at least one photo"));
            return new SyncPreflightResult(issues);
        }

        long primaryCount = photos.stream()
                .filter(Objects::nonNull)
                .filter(PhotoMetaDataV1::getPrimary)
                .count();
        if (primaryCount == 0) {
            issues.add(error(SyncPreflightIssueType.PRIMARY_PHOTO_MISSING, "Album manifest must identify one primary photo"));
        } else if (primaryCount > 1) {
            issues.add(error(SyncPreflightIssueType.MULTIPLE_PRIMARY_PHOTOS, "Album manifest contains multiple primary photos"));
        }

        issues.addAll(validateDuplicatePhotoIds(photos));
        photos.forEach(photo -> issues.addAll(validatePhoto(photo, false)));

        return new SyncPreflightResult(issues);
    }

    public SyncPreflightResult validatePhotoForUpload(PhotoMetaDataV1 photoMetaData) {
        return new SyncPreflightResult(validatePhoto(photoMetaData, false));
    }

    public SyncPreflightResult validatePhotoForReplace(PhotoMetaDataV1 photoMetaData) {
        return new SyncPreflightResult(validatePhoto(photoMetaData, true));
    }

    public SyncPreflightResult validateAlbumMembership(HostedAlbumMembershipRequest request) {
        List<SyncPreflightIssue> issues = new ArrayList<>();
        if (request == null) {
            issues.add(error(SyncPreflightIssueType.MEMBERSHIP_REQUIRED, "Album membership request is required"));
            return new SyncPreflightResult(issues);
        }

        if (isBlank(request.getAlbumId())) {
            issues.add(error(SyncPreflightIssueType.ALBUM_ID_REQUIRED, "Album id is required"));
        }
        if (isBlank(request.getPrimaryPhotoId())) {
            issues.add(error(SyncPreflightIssueType.PRIMARY_PHOTO_MISSING, "Primary photo id is required"));
        }

        Set<String> photoIds = Optional.ofNullable(request.getPhotoIds()).orElse(Set.of());
        if (photoIds.isEmpty()) {
            issues.add(error(SyncPreflightIssueType.MEMBERSHIP_HAS_NO_PHOTOS, "Album membership must contain at least one photo id"));
        }

        photoIds.stream()
                .filter(this::isBlank)
                .forEach(photoId -> issues.add(error(SyncPreflightIssueType.PHOTO_ID_MISSING, "Album membership contains a blank photo id")));

        if (!isBlank(request.getPrimaryPhotoId()) && !photoIds.contains(request.getPrimaryPhotoId())) {
            issues.add(error(SyncPreflightIssueType.PRIMARY_PHOTO_NOT_IN_MEMBERSHIP, "Primary photo id must be included in album membership")
                    .toBuilder()
                    .albumId(request.getAlbumId())
                    .photoId(request.getPrimaryPhotoId())
                    .build());
        }

        return new SyncPreflightResult(issues);
    }

    private List<SyncPreflightIssue> validatePhoto(PhotoMetaDataV1 photoMetaData, boolean requirePhotoId) {
        List<SyncPreflightIssue> issues = new ArrayList<>();
        if (photoMetaData == null) {
            issues.add(error(SyncPreflightIssueType.PHOTO_REQUIRED, "Photo metadata is required"));
            return issues;
        }

        String filename = Optional.ofNullable(photoMetaData.getFilename())
                .map(Path::toString)
                .orElse(null);
        if (isBlank(filename)) {
            issues.add(error(SyncPreflightIssueType.PHOTO_FILENAME_MISSING, "Photo filename is required"));
        }
        if (requirePhotoId && isBlank(photoMetaData.getPhotoId())) {
            issues.add(error(SyncPreflightIssueType.PHOTO_ID_MISSING, "Photo id is required before replacing a hosted photo")
                    .toBuilder()
                    .filename(filename)
                    .build());
        }

        Optional<Path> absolutePath = absolutePath(photoMetaData);
        if (absolutePath.isEmpty() || !Files.isRegularFile(absolutePath.get())) {
            issues.add(error(SyncPreflightIssueType.PHOTO_FILE_MISSING, "Photo file must exist before upload or replace")
                    .toBuilder()
                    .filename(filename)
                    .photoId(photoMetaData.getPhotoId())
                    .build());
        }

        return issues;
    }

    private Collection<SyncPreflightIssue> validateDuplicatePhotoIds(List<PhotoMetaDataV1> photos) {
        Map<String, PhotoMetaDataV1> seen = new HashMap<>();
        Collection<SyncPreflightIssue> issues = new ArrayList<>();
        for (PhotoMetaDataV1 photo : photos) {
            if (photo == null || isBlank(photo.getPhotoId())) {
                continue;
            }
            PhotoMetaDataV1 previous = seen.putIfAbsent(photo.getPhotoId(), photo);
            if (previous != null) {
                issues.add(error(SyncPreflightIssueType.DUPLICATE_PHOTO_ID, "Album manifest contains duplicate hosted photo ids")
                        .toBuilder()
                        .photoId(photo.getPhotoId())
                        .filename(Optional.ofNullable(photo.getFilename()).map(Path::toString).orElse(null))
                        .build());
            }
        }
        return issues;
    }

    private Optional<Path> absolutePath(PhotoMetaDataV1 photoMetaData) {
        if (photoMetaData.getPath() == null || photoMetaData.getFilename() == null) {
            return Optional.empty();
        }
        return Optional.of(photoMetaData.getAbsolutePath());
    }

    private SyncPreflightIssue error(SyncPreflightIssueType type, String message) {
        return SyncPreflightIssue.builder()
                .severity(SyncPreflightSeverity.ERROR)
                .type(type)
                .message(message)
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
