package com.vattima.lego.imaging.model;

import com.vattima.lego.imaging.util.PathUtils;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class FilePhotoSourceTest {

    @Test
    void getAbsolutePath() throws Exception {
        String sourceFilename = "DSC_1340.JPG";
        Path sourceAbsolutePath = PathUtils.fromClasspath("photo-source-directory", sourceFilename);
        FilePhotoSource filePhotoSource = new FilePhotoSource(sourceAbsolutePath.getParent(), sourceAbsolutePath.getFileName());

        Path absolutePath = filePhotoSource.getAbsolutePath();

        assertThat(absolutePath.getParent()).isEqualTo(sourceAbsolutePath.getParent());
        assertThat(absolutePath.getFileName()).isEqualTo(sourceAbsolutePath.getFileName());
    }

    @Test
    void inputStream() throws Exception {
        String sourceFilename = "DSC_0504.JPG";
        Path sourceAbsolutePath = PathUtils.fromClasspath("photo-source-directory", sourceFilename);
        FilePhotoSource filePhotoSource = new FilePhotoSource(sourceAbsolutePath.getParent(), sourceAbsolutePath.getFileName());

        assertThat(filePhotoSource.inputStream()).isNotEmpty();
    }

    @Test
    void uri() throws Exception {
        String sourceFilename = "DSC_1340.JPG";
        Path sourceAbsolutePath = PathUtils.fromClasspath("photo-source-directory", sourceFilename);
        FilePhotoSource filePhotoSource = new FilePhotoSource(sourceAbsolutePath.getParent(), sourceAbsolutePath.getFileName());

        assertThat(filePhotoSource.uri().toString()).endsWith("photo-source-directory" + "/" + sourceFilename);
    }

    @Test
    void move() throws Exception {
        String sourceFilename = "DSC_0691.JPG";
        Path sourceAbsolutePath = PathUtils.fromClasspath("photo-source-directory", sourceFilename);
        FilePhotoSource filePhotoSource = new FilePhotoSource(sourceAbsolutePath.getParent(), sourceAbsolutePath.getFileName());

        Path targetAbsolutePath = PathUtils.fromClasspath("", "photo-target-directory");
        PhotoSource movedFilePhotoSource = filePhotoSource.move(targetAbsolutePath.toUri());
        URI targetUri = movedFilePhotoSource.uri();
        Path targetPath = Paths.get(targetUri);

        assertThat(targetPath).isEqualTo(targetAbsolutePath);
    }
}