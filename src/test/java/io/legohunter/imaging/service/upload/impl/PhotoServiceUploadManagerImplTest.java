package io.legohunter.imaging.service.upload.impl;

import io.legohunter.imaging.bitly.impl.BitlinksService;
import io.legohunter.imaging.config.LegoImagingProperties;
import io.legohunter.imaging.service.album.api.AlbumManager;
import io.legohunter.imaging.service.hosting.api.ImageHostingService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PhotoServiceUploadManagerImplTest {
    @Test
    void canConstructWithCurrentFacadeDependencies() {
        PhotoServiceUploadManagerImpl uploadManager = new PhotoServiceUploadManagerImpl(
                mock(ImageHostingService.class),
                new LegoImagingProperties(),
                mock(AlbumManager.class),
                mock(BitlinksService.class)
        );

        assertThat(uploadManager).isNotNull();
    }
}
