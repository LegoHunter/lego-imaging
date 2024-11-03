package com.vattima.lego.imaging.model;

import com.vattima.lego.imaging.LegoImagingException;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlbumManifestTest {

    @Test
    void getPrimaryPhoto_whenMultiplePhotosWithOnePrimary_returnsOne() {
        AlbumManifest albumManifest = new AlbumManifest();
        PhotoMetaData pmd1 = new PhotoMetaData(Paths.get("a.jpg"));
        pmd1.setPhotoId("1");
        pmd1.setPrimary(false);
        albumManifest.getPhotos()
                     .add(pmd1);

        PhotoMetaData pmd2 = new PhotoMetaData(Paths.get("b.jpg"));
        pmd2.setPhotoId("2");
        pmd2.setPrimary(true);
        albumManifest.getPhotos()
                     .add(pmd2);

        PhotoMetaData pmd3 = new PhotoMetaData(Paths.get("c.jpg"));
        pmd3.setPhotoId("3");
        pmd3.setPrimary(false);
        albumManifest.getPhotos()
                     .add(pmd3);

        PhotoMetaData primary = albumManifest.getPrimaryPhoto();
        assertThat(primary).isSameAs(pmd2);

        assertThat(albumManifest.hasPrimaryPhoto()).isTrue();
    }

    @Test
    void getPrimaryPhoto_whenMultiplePhotosWithZeroPrimary_returnsFirstPhoto() {
        AlbumManifest albumManifest = new AlbumManifest();
        PhotoMetaData pmd1 = new PhotoMetaData(Paths.get("a.jpg"));
        pmd1.setPhotoId("1");
        pmd1.setPrimary(false);
        albumManifest.getPhotos()
                     .add(pmd1);

        PhotoMetaData pmd2 = new PhotoMetaData(Paths.get("b.jpg"));
        pmd2.setPhotoId("2");
        pmd2.setPrimary(false);
        albumManifest.getPhotos()
                     .add(pmd2);

        PhotoMetaData pmd3 = new PhotoMetaData(Paths.get("c.jpg"));
        pmd3.setPhotoId("3");
        pmd3.setPrimary(false);
        albumManifest.getPhotos()
                     .add(pmd3);

        PhotoMetaData primary = albumManifest.getPrimaryPhoto();
        assertThat(primary).isSameAs(pmd1);
        assertThat(albumManifest.hasPrimaryPhoto()).isFalse();
    }

    @Test
    void getPrimaryPhoto_whenZeroPhotos() {
        AlbumManifest albumManifest = new AlbumManifest();
        assertThatThrownBy(albumManifest::getPrimaryPhoto).isInstanceOf(LegoImagingException.class)
                                                          .hasMessageStartingWith("No photos exist from which to select a primary photo");
        assertThat(albumManifest.hasPrimaryPhoto()).isFalse();
    }

    @Test
    void fromJson_asString() {
        String json = "{\"AlbumManifest\":{\"photosetId\":\"1982736941923\",\"title\":\"The title\",\"description\":\"The description\",\"url\":\"https://www.bogus.com/photoset/1982736941923\",\"uuid\":\"fdaa0638814727a42f005656f38b92c6\",\"blItemNumber\":\"1234-1\",\"photos\":[{\"path\":\".\",\"filename\":\"DSC_0001.jpg\",\"md5\":\"ABC123\",\"photoId\":\"01982395801283923\",\"uploadReturnCode\":0,\"uploadedTimeStamp\":\"2019-03-30T16:15:23.125\",\"primary\":true},{\"path\":\".\",\"filename\":\"DSC_0002.jpg\",\"md5\":\"XYZ987\",\"photoId\":\"232403948702304723\",\"uploadReturnCode\":0,\"uploadedTimeStamp\":\"2019-03-30T16:15:23.131\",\"primary\":false},{\"path\":\".\",\"filename\":\"DSC_0003.jpg\",\"md5\":\"JKL456\",\"photoId\":\"209384702342873\",\"uploadReturnCode\":0,\"uploadedTimeStamp\":\"2019-03-30T16:15:23.131\",\"primary\":false}],\"new\":true}}";
        AlbumManifest albumManifest = AlbumManifest.fromJson(json);
        assertThat(albumManifest.getTitle()).isEqualTo("The title");
        assertThat(albumManifest.getDescription()).isEqualTo("The description");
        assertThat(albumManifest.getPhotosetId()).isEqualTo("1982736941923");
        assertThat(albumManifest.getPhotos()).hasSize(3);
        assertThat(albumManifest.getPrimaryPhoto().getPhotoId()).isEqualTo("01982395801283923");
    }

    @Test
    void fromJson_withNonExistentFile_returnsNewEmptyAlbumManifest() {
        AlbumManifest albumManifest = AlbumManifest.fromJson(Paths.get("bogus-xxxxxxx"));

        assertThatThrownBy(albumManifest::getTitle).isInstanceOf(LegoImagingException.class).hasMessageContaining("No");
        assertThatThrownBy(albumManifest::getDescription).isInstanceOf(LegoImagingException.class).hasMessageContaining("No");
        assertThat(albumManifest.getPhotosetId()).isBlank();
        assertThat(albumManifest.isNew()).isTrue();
        assertThat(albumManifest.getPhotos()).hasSize(0);
        assertThatThrownBy(albumManifest::getPrimaryPhoto).isInstanceOf(LegoImagingException.class)
                                                          .hasMessageStartingWith("No photos exist from which to select a primary photo");

    }

    @Test
    void toAndFromJson() {
        AlbumManifest outputAlbumManifest = new AlbumManifest();
        outputAlbumManifest.setTitle("The title");
        outputAlbumManifest.setDescription("The description");
        outputAlbumManifest.setBlItemNumber("1234-1");
        outputAlbumManifest.setNew(true);
        outputAlbumManifest.setPhotosetId("1982736941923");
        try {
            outputAlbumManifest.setUrl(new URL("https://www.bogus.com/photoset/1982736941923"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        outputAlbumManifest.setUuid("fdaa0638814727a42f005656f38b92c6");

        PhotoMetaData pmd = new PhotoMetaData(Paths.get("c:\\temp\\a.jpg"));
        pmd.setPhotoId("01982395801283923");
        pmd.setPrimary(true);
        pmd.setMd5("ABC123");
        pmd.setUploadedTimeStamp(LocalDateTime.now());
        pmd.setUploadReturnCode(0);
        outputAlbumManifest.getPhotos().add(pmd);

        pmd = new PhotoMetaData(Paths.get("c:\\temp\\b.jpg"));
        pmd.setPhotoId("232403948702304723");
        pmd.setPrimary(false);
        pmd.setMd5("XYZ987");
        pmd.setUploadedTimeStamp(LocalDateTime.now());
        pmd.setUploadReturnCode(0);
        outputAlbumManifest.getPhotos().add(pmd);

        pmd = new PhotoMetaData(Paths.get("c:\\temp\\c.jpg"));
        pmd.setPhotoId("209384702342873");
        pmd.setPrimary(false);
        pmd.setMd5("JKL456");
        pmd.setUploadedTimeStamp(LocalDateTime.now());
        pmd.setUploadReturnCode(0);
        outputAlbumManifest.getPhotos().add(pmd);

        File f = Files.newTemporaryFile();
        AlbumManifest.toJson(f.toPath(), outputAlbumManifest);

        AlbumManifest inputAlbumManifest = AlbumManifest.fromJson(f.toPath());
        assertThat(inputAlbumManifest).isEqualTo(outputAlbumManifest);
    }
}