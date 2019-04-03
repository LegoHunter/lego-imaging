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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.AbstractMap.SimpleEntry;
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

    public Function<URL, ImageMetadata> getImageMetadata() {
        return this.getImageMetadata;
    }

    public Function<ImageMetadata, Stream<ImageMetadataItem>> getJpgImageMetadataItems() {
        return jpgImageMetadataItems;
    }

    public Predicate<ImageMetadataItem> getKeywordsFilter() {
        return keywordsFilter;
    }

    public Function<ImageMetadataItem, Stream<Map.Entry<String, String>>> getKeywordsExtractor() {
        return keywordsExtractor;
    }

    private LegoImagingProperties getLegoImagingProperties() {
        return legoImagingProperties;
    }

    Function<URL, ImageMetadata> getImageMetadata = u -> {
        try {
            return Imaging.getMetadata(new File(u.toURI()));
        } catch (ImageReadException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    };

    Function<ImageMetadata, Stream<ImageMetadataItem>> jpgImageMetadataItems = m -> {
        if (m instanceof JpegImageMetadata) {
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) m;
            return jpegMetadata.getItems()
                               .stream();
        } else {
            return Stream.empty();
        }
    };

    Predicate<ImageMetadataItem> keywordsFilter = m -> Optional.of(m.toString())
                                                               .map(s -> s.startsWith(getLegoImagingProperties().getKeywordsKeyName()))
                                                               .orElse(false);

    Function<ImageMetadataItem, Stream<Map.Entry<String, String>>> keywordsExtractor = m -> Keywords.of(getLegoImagingProperties().getKeywordsKeyName(), m.toString())
                                                                                                    .map(Keywords.tokenizer);

    static class Keywords {
        private static String pairDelimiter = "[;,\\s]";

        static Stream<String> of(String keywordsKeyName, String keywordString, String pairDelimiter) {
            final Pattern pattern = Pattern.compile("^" + keywordsKeyName + "\\s+?'?(.*?)'?$");
            Matcher matcher = pattern.matcher(keywordString);
            if (matcher.find()) {
                return Arrays.stream(matcher.group(1)
                                            .split(pairDelimiter));
            } else {
                return Stream.empty();
            }
        }

        static Stream<String> of(String keywordsKeyName, String keywordString) {
            return of(keywordsKeyName, keywordString, pairDelimiter);
        }

        static Function<String, SimpleEntry<String, String>> tokenizer = s -> {
            String[] tokens = new String[]{"", ""};
            if (s.contains(":")) {
                tokens = s.split(":");
            } else if (s.contains("=")) {
                tokens = s.split("=");
            } else {
                tokens[0] = s;
                tokens[1] = s;
            }
            return new SimpleEntry<>(tokens[0], (tokens.length==2)?tokens[1]:"");
        };
    }
}
