package com.vattima.lego.imaging.util;

import org.springframework.util.ResourceUtils;

import java.nio.file.Path;

public final class PathUtils {
    private PathUtils() {
    }

    public static Path fromClasspath(String pathname, String filename) throws Exception {
        return fromClasspath(pathname).resolve(filename);
    }

    public static Path fromClasspath(String pathname) throws Exception {
        return Path.of(ResourceUtils.getURL("classpath:" + pathname).toURI());
    }
}
