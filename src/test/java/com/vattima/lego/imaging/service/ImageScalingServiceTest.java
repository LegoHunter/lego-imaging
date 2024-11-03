package com.vattima.lego.imaging.service;

import com.vattima.lego.imaging.LegoImagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import static java.nio.file.FileVisitResult.CONTINUE;

@Slf4j
class ImageScalingServiceTest {
    @Test
    void scale() throws Exception {
        try {
            ImageScalingService imageScalingService = new ImageScalingService();
            Stream.Builder<Path> builder = Stream.builder();
            Files.walkFileTree(Path.of("D:\\data\\lego\\lego-collection-photos"), new JpgFileVisitor(builder));
            Stream<Path> paths = builder.build();
            paths.limit(10).forEach(p -> {
                log.info("path [{}]", p);
                Path tempPath = imageScalingService.scale(p);
                log.info("Scaled [{}] to [{}]", p, tempPath);
            });
        } catch (IOException e) {
            throw new LegoImagingException(e);
        }
    }

    @Test
    void scale_downloadsURL_returnsTempFile() throws Exception {
        ImageScalingService imageScalingService = new ImageScalingService();
        Path path = imageScalingService.scale(new URL("https://farm66.static.flickr.com/65535/49417636867_c433dc6ab2_c.jpg"));
        System.out.println(path);
    }

    @RequiredArgsConstructor
    private static class JpgFileVisitor extends SimpleFileVisitor<Path> {
        private final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:DSC_1340.jpg");
        private final Stream.Builder<Path> builder;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (matcher.matches(file.getFileName())) {
                builder.add(file);
            }
            return CONTINUE;
        }
    }
}