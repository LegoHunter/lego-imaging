package com.vattima.lego.imaging.service;

import com.vattima.lego.imaging.model.PhotoMetaData;

import java.util.Map;

public interface ImageManager {
    String computeMD5Hash(final PhotoMetaData photoMetaData);
    void extractKeywords(final PhotoMetaData photoMetaData);
}
