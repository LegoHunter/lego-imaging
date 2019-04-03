package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.file.ImageCollector;
import com.vattima.lego.imaging.flickr.configuration.FlickrProperties;
import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.ImageFileHolder;
import com.vattima.lego.imaging.service.AlbumManager;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import org.junit.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AlbumManagerImplTest {

    @Test
    public void addPhoto() {
        FlickrProperties flickrProperties = new FlickrProperties();
        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
        ImageCollector imageCollector = new ImageCollector(legoImagingProperties);
        BricklinkInventoryDao bricklinkInventoryDao = mock(BricklinkInventoryDao.class);

        AlbumManager albumManager = new AlbumManagerImpl(legoImagingProperties, imageCollector, flickrProperties, bricklinkInventoryDao);
        ImageFileHolder imageFileHolder = new ImageFileHolder(Paths.get("."), imageCollector, bricklinkInventoryDao, legoImagingProperties);
        AlbumManifest albumManifest = albumManager.addPhoto(imageFileHolder);
        assertThat(albumManifest).isNotNull();

    }
}