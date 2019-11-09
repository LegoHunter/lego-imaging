package com.vattima.lego.imaging.service;

import com.vattima.lego.imaging.model.PhotoMetaData;

import java.util.Map;

public interface ImageManager {
    Map<String, String> getKeywords(final PhotoMetaData photoMetaData);
}
