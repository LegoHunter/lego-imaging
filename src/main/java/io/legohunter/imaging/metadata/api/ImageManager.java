package io.legohunter.imaging.metadata.api;

import io.legohunter.imaging.model.PhotoMetaDataV1;

import java.util.Map;

public interface ImageManager {
    Map<String, String> getKeywords(final PhotoMetaDataV1 photoMetaData);
}
