package com.vattima.lego.imaging.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;

@RequiredArgsConstructor
@Slf4j
public class FilePhotoSource implements PhotoSource {
    private final Path path;
    private final Path filename;

    private InputStream inputStream;

    public Path getAbsolutePath() {
        return path.resolve(filename);
    }

    @Override
    public InputStream inputStream() throws IOException {
        if (null == inputStream) {
            inputStream = Files.newInputStream(this.getAbsolutePath());
        }
        return inputStream;
    }

    @Override
    public URI uri() throws URISyntaxException {
        return getAbsolutePath().toUri();
    }

    @Override
    public PhotoSource move(URI uri) throws IOException {
        Path targetDirectory = Paths.get(uri);
        if (!Files.exists(targetDirectory, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectory(targetDirectory);
            log.debug("Created directory path [{}]", targetDirectory);
        }
        Path targetFile = targetDirectory.resolve(filename.toString());
        if (Files.exists(targetFile, LinkOption.NOFOLLOW_LINKS)) {
            Files.delete(targetFile);
            log.debug("Deleted existing image [{}]", targetFile);
        }
        log.info("Moving image from [{}] to [{}]", getAbsolutePath(), targetFile);
        Files.copy(getAbsolutePath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Copied image from [{}] to [{}]", getAbsolutePath(), targetFile);
        Files.delete(getAbsolutePath());
        log.debug("Deleted old image [{}]", getAbsolutePath());
        return new FilePhotoSource(targetDirectory.getParent(), targetDirectory.getFileName());
    }
}
