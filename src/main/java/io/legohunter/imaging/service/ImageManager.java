package io.legohunter.imaging.service;

import io.legohunter.imaging.model.PhotoMetaData;

import java.util.Map;

public interface ImageManager {
    Map<String, String> getKeywords(final PhotoMetaData photoMetaData);
}
