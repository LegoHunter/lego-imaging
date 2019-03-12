package com.vattima.lego.imaging.file;

import com.vattima.lego.imaging.config.LegoImagingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;

@Component
@Slf4j
@RequiredArgsConstructor
public class ImageCollector {
    private final LegoImagingProperties legoImagingProperties;

    public List<Path> getImagePaths() {
        List<Path> imagePaths = new ArrayList<>();
        Path rootImagingDirectory = Paths.get(legoImagingProperties.getRootImagesFolder());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootImagingDirectory, "*.jpg")) {
            for (Path filePath : stream) {
                imagePaths.add(filePath);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.info("Found [{}] image paths in location [{}]", imagePaths.size(), rootImagingDirectory.toFile()
                                                                                                   .getAbsolutePath());
        return imagePaths;
    }

    public Function<URL, ImageMetadata> getImageMetadata = u -> {
        try {
            return Imaging.getMetadata(new File(u.toURI()));
        } catch (ImageReadException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    };

    public Function<ImageMetadata, Stream<ImageMetadataItem>> jpgImageMetadataItems = m -> {
        if (m instanceof JpegImageMetadata) {
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) m;
            return jpegMetadata.getItems()
                               .stream();
        } else {
            return Stream.empty();
        }
    };

    public Predicate<ImageMetadataItem> keywordsFilter = m -> Optional.of(m.toString())
                                                                      .map(s -> s.startsWith(getLegoImagingProperties().getKeywordsKeyName()))
                                                                      .orElse(false);

    public LegoImagingProperties getLegoImagingProperties() {
        return legoImagingProperties;
    }

    public Function<ImageMetadataItem, Map<String, String>> keywordsExtractor = m -> Collections.emptyMap();

//    public Map<String, String> getKeywords(Path path) {
//        // get all metadata stored in EXIF format (ie. from JPEG or TIFF).
//        File file = path.toFile();
//        final ImageMetadata metadata;
//        Map<String, String> keywords = new HashMap<>();
//        try {
//            metadata = Imaging.getMetadata(file);
//            if (metadata instanceof JpegImageMetadata) {
//                final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
//
//                return keywordsExtractor.apply(jpegMetadata.getItems());
//                keywords = jpegMetadata.getItems()
//                                       .stream()
//                                       .map(keywordsExtractor)
//                                       .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1], (k, v) -> v));
//            }
//        } catch (ImageReadException | IOException e) {
//            log.error(e.getMessage(), e);
//        }
//        return keywords;
//    }
}
