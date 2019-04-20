package com.vattima.lego.imaging.util;

import org.springframework.util.ResourceUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathUtils {
    private PathUtils() {
    }

    public static Path fromClasspath(String pathname, String filename) throws Exception {
        return fromClasspath(pathname).resolve(filename);
    }

    public static Path fromClasspath(String pathname) throws Exception {
        return Paths.get(ResourceUtils.getURL("classpath:" + pathname).toURI());
    }
}
