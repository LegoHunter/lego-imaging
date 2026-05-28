package io.legohunter.imaging.service.upload.impl;

import io.legohunter.imaging.config.LegoImagingProperties;
import io.legohunter.imaging.exception.LegoImagingException;
import io.legohunter.imaging.model.AlbumManifest;
import io.legohunter.imaging.model.HostedAlbum;
import io.legohunter.imaging.model.HostedAlbumCreateRequest;
import io.legohunter.imaging.model.HostedAlbumMembershipRequest;
import io.legohunter.imaging.model.PhotoMetaDataV1;
import io.legohunter.imaging.bitly.model.bitly.ShortenRequest;
import io.legohunter.imaging.bitly.impl.BitlinksService;
import io.legohunter.imaging.model.PhotoServiceResponse;
import io.legohunter.imaging.model.SimplePhotoServiceRequest;
import io.legohunter.imaging.service.album.api.AlbumManager;
import io.legohunter.imaging.service.hosting.api.ImageHostingService;
import io.legohunter.imaging.service.upload.api.PhotoServiceUploadManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class PhotoServiceUploadManagerImpl implements PhotoServiceUploadManager {
    private final ImageHostingService imageHostingService;
    private final LegoImagingProperties legoImagingProperties;
    private final AlbumManager albumManager;
    private final BitlinksService bitlinksService;

    private Set<AlbumManifest> albumManifests = new HashSet<>();

    @Override
    public void updateAll() {
        final Path root = Paths.get(legoImagingProperties.getRootImagesFolder());
        albumManager.findManifests(root).forEach(a -> {
            // Upload all new/changed photos
            a.getPhotos()
                    .forEach(pmd -> {
                        try {
                            if (Optional.ofNullable(pmd.getPhotoId())
                                    .isPresent()) {
                                if (pmd.isChanged()) {
                                    log.info("Uploading changed photo [{}]", pmd);
                                    StopWatch timer = new StopWatch();
                                    timer.start();
                                    PhotoServiceResponse<String> response = imageHostingService.replacePhoto(new SimplePhotoServiceRequest<>(pmd));
                                    assertSuccess(response, pmd);
                                    timer.stop();
                                    pmd.setPhotoId(response.get());
                                    pmd.setChanged(false);
                                    log.info("Uploaded changed photo [{}] in [{}] ms", pmd, timer.getTotalTimeMillis());
                                } else {
                                    log.debug("Photo not changed [{}]", pmd);
                                }
                            } else {
                                log.info("Uploading new [{}]", pmd);
                                StopWatch timer = new StopWatch();
                                timer.start();
                                PhotoServiceResponse<String> response = imageHostingService.uploadPhoto(new SimplePhotoServiceRequest<>(pmd));
                                assertSuccess(response, pmd);
                                timer.stop();
                                pmd.setUploadReturnCode(0);
                                pmd.setUploadedTimeStamp(LocalDateTime.now());
                                pmd.setPhotoId(response.get());
                                pmd.setChanged(false);
                                log.info("Uploaded new [{}] in [{}] ms", pmd, timer.getTotalTimeMillis());
                            }
                        } catch (LegoImagingException e) {
                            pmd.setUploadReturnCode(-1);
                            throw e;
                        }
                    });
            // Create the Album if it doesn't exist
            PhotoMetaDataV1 primaryPhoto = a.getPrimaryPhoto();
            String primaryPhotoId = primaryPhoto.getPhotoId();
            String photosetId = Optional.ofNullable(a.getPhotosetId())
                    .orElseGet(() -> {
                        try {
                            PhotoServiceResponse<HostedAlbum> response = imageHostingService.createAlbum(new SimplePhotoServiceRequest<>(toCreateRequest(a)));
                            assertSuccess(response);
                            HostedAlbum album = response.get();
                            log.info("Created Photoset [{}] with primary photo id [{}] - filename [{}]", album, primaryPhotoId, primaryPhoto.getFilename());
                            a.setPhotosetId(album.getId());
                            a.setUrl(new URL(album.getUrl()));
                            ShortenRequest shortenRequest = new ShortenRequest();
                            shortenRequest.setLongUrl(a.getUrl().toExternalForm());
                            a.setShortUrl(new URL(bitlinksService.shorten(shortenRequest).getLink()));
                            return a.getPhotosetId();
                        } catch (MalformedURLException e) {
                            throw new LegoImagingException(e);
                        }
                    });
            PhotoServiceResponse<Void> response = imageHostingService.updateAlbumMembership(new SimplePhotoServiceRequest<>(HostedAlbumMembershipRequest.builder()
                    .albumId(a.getPhotosetId())
                    .primaryPhotoId(primaryPhotoId)
                    .photoIds(new LinkedHashSet<>(a.getPhotoIds()))
                    .build()));
            if (response.isError()) {
                log.error(response.responseMessage());
            } else {
                log.info("Updated PhotosetId [{}] with primary photo id [{}] - added photos {}", photosetId, primaryPhoto.getPhotoId(), a.getPhotoIdsArray());
            }
            AlbumManifest.toJson(a.getAlbumManifestFile(root), a);
        });
        albumManifests.clear();
    }

    private void assertSuccess(PhotoServiceResponse<?> response, PhotoMetaDataV1 photoMetaData) {
        if (response.isError()) {
            photoMetaData.setUploadReturnCode(response.responseCode());
            throw new LegoImagingException(response.responseMessage());
        }
    }

    private void assertSuccess(PhotoServiceResponse<?> response) {
        if (response.isError()) {
            throw new LegoImagingException(response.responseMessage());
        }
    }

    private HostedAlbumCreateRequest toCreateRequest(AlbumManifest albumManifest) {
        PhotoMetaDataV1 primaryPhoto = albumManifest.getPrimaryPhoto();
        return HostedAlbumCreateRequest.builder()
                .title(albumManifest.getTitle())
                .description(albumManifest.getDescription())
                .primaryPhotoId(primaryPhoto.getPhotoId())
                .photoIds(albumManifest.getPhotoIds())
                .build();
    }
}
