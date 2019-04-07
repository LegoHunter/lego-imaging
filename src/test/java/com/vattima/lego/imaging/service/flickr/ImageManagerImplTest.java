package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.ImageManager;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageManagerImplTest {

    @Test
    public void computeMD5Hash() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords/DSC_0504.JPG").toURI());
        ImageManager imageManager = new ImageManagerImpl();
        PhotoMetaData photoMetaData = new PhotoMetaData(jpgPath);
        String md5Hash = imageManager.computeMD5Hash(photoMetaData);
        assertThat(md5Hash).isEqualTo("23BFB61B48D367368A03CBC0028C38EB");
    }
}