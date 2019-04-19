package com.vattima.lego.imaging.service;

import com.vattima.lego.imaging.model.PhotoMetaData;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Map;

public interface ImageManager {
    String computeMD5Hash(final PhotoMetaData photoMetaData);
    Map<String, String> getKeywords(final PhotoMetaData photoMetaData);
    DirectoryStream<Path> imagePaths(final Path path);
}
