package com.vattima.lego.imaging.service;

import com.vattima.lego.imaging.LegoImagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import static java.nio.file.FileVisitResult.CONTINUE;

@Slf4j
public class ImageScalingServiceTest {
    @Test
    public void scale() throws Exception {
        try {
            ImageScalingService imageScalingService = new ImageScalingService();
            Stream.Builder<Path> builder = Stream.builder();
            Files.walkFileTree(Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos - all"), new JpgFileVisitor(builder));
            Stream<Path> paths = builder.build();
            paths.limit(10).forEach(p -> {
                log.info("path [{}]", p);
                imageScalingService.scale(p);
            });
        } catch (IOException e) {
            throw new LegoImagingException(e);
        }
    }

    @RequiredArgsConstructor
    private static class JpgFileVisitor extends SimpleFileVisitor<Path> {
        private final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.jpg");
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