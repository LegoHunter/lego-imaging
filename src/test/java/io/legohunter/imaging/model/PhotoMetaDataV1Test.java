package io.legohunter.imaging.model;

import io.legohunter.imaging.exception.LegoImagingException;
import io.legohunter.imaging.util.PathUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhotoMetaDataV1Test {

    @Test
    void getMd5_returnsCorrectMd5Hash() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("photo-metadata-test", "DSC_0504.JPG");
        PhotoMetaDataV1 photoMetaData = new PhotoMetaDataV1(jpgPath);
        String md5Hash = photoMetaData.getMd5();
        assertThat(md5Hash).isEqualTo("23BFB61B48D367368A03CBC0028C38EB");
    }

    @Test
    void getMd5_withNonExistentPath_throwsException() throws Exception {
        String expectedNameOfNonExistingFile = "bogus-file-that-does-not-exist.JPG";
        Path jpgPath = PathUtils.fromClasspath("photo-metadata-test", expectedNameOfNonExistingFile);
        PhotoMetaDataV1 photoMetaData = new PhotoMetaDataV1(jpgPath);
        assertThatThrownBy(photoMetaData::getMd5).isInstanceOf(LegoImagingException.class)
                                                 .hasCauseInstanceOf(NoSuchFileException.class)
                                                 .hasMessageContaining(expectedNameOfNonExistingFile);
    }

    @Test
    void equals_isFalse_withTwoJPGs_withSameFilenames_withDifferentMD5Hashes() throws Exception {
        Path folder1_DSC_0504_JPG = PathUtils.fromClasspath("photo-metadata-test/equals-hashcode-test/folder-1", "DSC_0504.JPG");
        Path folder2_DSC_0504_JPG = PathUtils.fromClasspath("photo-metadata-test/equals-hashcode-test/folder-2", "DSC_0504.JPG");
        PhotoMetaDataV1 photoMetaData_folder1_DSC_0504_JPG = new PhotoMetaDataV1(folder1_DSC_0504_JPG);
        PhotoMetaDataV1 photoMetaData_folder2_DSC_0504_JPG = new PhotoMetaDataV1(folder2_DSC_0504_JPG);
        assertThat(photoMetaData_folder1_DSC_0504_JPG).isNotEqualTo(photoMetaData_folder2_DSC_0504_JPG);
        assertThat(photoMetaData_folder1_DSC_0504_JPG.hashCode()).isNotEqualTo(photoMetaData_folder2_DSC_0504_JPG.hashCode());
    }

    @Test
    void equals_isTrue_withTwoJPGs_withDifferentFilenames_withSameMD5Hashes() throws Exception {
        Path folder1_DSC_0504_JPG = PathUtils.fromClasspath("photo-metadata-test/equals-hashcode-test/folder-1", "DSC_0504.JPG");
        Path folder2_DSC_0504_JPG = PathUtils.fromClasspath("photo-metadata-test/equals-hashcode-test/folder-2", "DSC_0504-copy-with-different-md5-hash.JPG");
        PhotoMetaDataV1 photoMetaData_folder1_DSC_0504_JPG = new PhotoMetaDataV1(folder1_DSC_0504_JPG);
        PhotoMetaDataV1 photoMetaData_folder2_DSC_0504_JPG = new PhotoMetaDataV1(folder2_DSC_0504_JPG);
        assertThat(photoMetaData_folder1_DSC_0504_JPG).isEqualTo(photoMetaData_folder2_DSC_0504_JPG);
        assertThat(photoMetaData_folder2_DSC_0504_JPG.hashCode()).isEqualTo(photoMetaData_folder2_DSC_0504_JPG.hashCode());
    }

    @Test
    void setKeywords_addsUniqueExtraDescription() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("photo-metadata-test", "DSC_0504.JPG");
        PhotoMetaDataV1 photoMetaData = new PhotoMetaDataV1(jpgPath);

        Map<String, String> keywords = new HashMap<>();
        keywords.put("cp", "this is extra description #1");
        photoMetaData.setKeywords(keywords);
        assertThat(photoMetaData.getKeywords()).containsEntry("cp", "this is extra description #1");

        keywords.put("cp", "this is extra description #2");
        photoMetaData.setKeywords(keywords);
        assertThat(photoMetaData.getKeywords()).containsEntry("cp", "this is extra description #2");

        keywords.put("cp", "this is the third extra description that are different than the first two");
        photoMetaData.setKeywords(keywords);
        assertThat(photoMetaData.getKeywords()).containsEntry("cp", "this is the third extra description that are different than the first two");
    }
}