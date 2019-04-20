package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.util.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Map;

import static com.vattima.lego.imaging.service.flickr.ImageManagerImpl.KeywordsSplitter;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ImageManagerImplTest {
    @Test
    public void getKeywords_withJpgFileThatHasKeywords() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("jpgs", "jpeg-with-keywords.jpg");
        ImageManagerImpl imageManager = new ImageManagerImpl();
        PhotoMetaData photoMetaData = new PhotoMetaData(jpgPath);
        Map<String, String> map = imageManager.getKeywords(photoMetaData, "XPKeywords:");
        assertThat(map).isNotNull();
        assertThat(map.keySet()).hasSize(4);
        assertThat(map).containsOnlyKeys("tag1", "tag2", "b", "tag3");
        assertThat(map).containsEntry("tag1", "a");
        assertThat(map).containsEntry("tag2", "tag2");
        assertThat(map).containsEntry("b", "b");
        assertThat(map).containsEntry("tag3", "123");
    }

    @Test
    public void getKeywords_withJpgFileThatHasNoKeywords_returnsEmptyMap() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("jpgs", "jpeg-without-keywords.jpg");
        ImageManagerImpl imageManager = new ImageManagerImpl();
        PhotoMetaData photoMetaData = new PhotoMetaData(jpgPath);
        Map<String, String> map = imageManager.getKeywords(photoMetaData, "XPKeywords:");
        assertThat(map).isNotNull();
        assertThat(map.keySet()).hasSize(0);
    }

    @Test
    public void getKeywords_withJpgFilesThatHaveKeywords_returnANonEmptyMap() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("actual-lego-photos-with-keywords");
        ImageManagerImpl imageManager = new ImageManagerImpl();
        DirectoryStream<Path> paths = Files.newDirectoryStream(jpgPath, "*.jpg");
        paths.forEach(p -> {
            try {
                PhotoMetaData photoMetaData = new PhotoMetaData(p);
                Map<String, String> map = imageManager.getKeywords(photoMetaData, "Keywords:");
                assertThat(map).isNotNull();
                assertThat(map.keySet()).isNotNull();
                assertThat(map.keySet()
                              .size()).isGreaterThan(0);
                log.info("path=[{}], map=[{}]", p, map);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void keywordsSplitter_of_parsesKeywords() {
        assertThat(KeywordsSplitter.of("Keywords:", "Keywords: a:1;b:2;c:3")).containsExactly("a:1", "b:2", "c:3");
        assertThat(KeywordsSplitter.of("Keywords:", "Keywords:a:1;b:2;c:3")).containsExactly("a:1", "b:2", "c:3");
        assertThat(KeywordsSplitter.of("Keywords: ", "Keywords: a:1;b:2;c:3")).containsExactly("a:1", "b:2", "c:3");
        assertThat(KeywordsSplitter.of("Keywords: ", "Keywords:a:1;b:2;c:3")).isEmpty();
        assertThat(KeywordsSplitter.of("foo: ", "Keywords:a:1;b:2;c:3")).isEmpty();

        assertThat(KeywordsSplitter.of("Keywords:", "Keywords: a:1,b:2,c:3")).containsExactly("a:1", "b:2", "c:3");
        assertThat(KeywordsSplitter.of("Keywords:", "Keywords: a:1 b:2 c:3")).containsExactly("a:1", "b:2", "c:3");
        assertThat(KeywordsSplitter.of("Keywords:", "Keywords: a:1;b:2,c:3")).containsExactly("a:1", "b:2", "c:3");
        assertThat(KeywordsSplitter.of("Keywords:", "Keywords: a:1;b:2,c:3 d:4")).containsExactly("a:1", "b:2", "c:3", "d:4");
        assertThat(KeywordsSplitter.of("Keywords:", "Keywords: a;b:2,c d:4")).containsExactly("a", "b:2", "c", "d:4");
        assertThat(KeywordsSplitter.of("Keywords:", "Keywords: a;b:2,c d=4")).containsExactly("a", "b:2", "c", "d=4");
    }

    @Test
    public void keywordsSplitter_tokenizer_returnsMapEntry() {
        assertThat(KeywordsSplitter.tokenizer.apply("a:1")).isEqualTo(new AbstractMap.SimpleEntry<>("a", "1"));
        assertThat(KeywordsSplitter.tokenizer.apply("a=1")).isEqualTo(new AbstractMap.SimpleEntry<>("a", "1"));
        assertThat(KeywordsSplitter.tokenizer.apply("a,1")).isEqualTo(new AbstractMap.SimpleEntry<>("a,1", "a,1"));
        assertThat(KeywordsSplitter.tokenizer.apply("a")).isEqualTo(new AbstractMap.SimpleEntry<>("a", "a"));
        assertThat(KeywordsSplitter.tokenizer.apply("a=1=2")).isEqualTo(new AbstractMap.SimpleEntry<>("a", "1=2"));
        assertThat(KeywordsSplitter.tokenizer.apply("a=1:2")).isEqualTo(new AbstractMap.SimpleEntry<>("a", "1:2"));
    }
}