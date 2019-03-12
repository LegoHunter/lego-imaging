package com.vattima.lego.imaging.handler;

import java.util.Map;
import java.util.function.Function;

import static org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;

public interface MetadataExtractor extends Function<ImageMetadataItem, Map<String, String>> {
}
