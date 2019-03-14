package com.vattima.lego.imaging.model;

import com.vattima.lego.imaging.LegoImagingException;
import com.vattima.lego.imaging.file.ImageCollector;
import lombok.*;
import org.apache.commons.imaging.common.ImageMetadata;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class ImageFileHolder {
    private final Path path;
    private final ImageCollector imageCollector;
    Map<String, String> keywords;

    public boolean hasUuid() {
        getKeywords();
        return keywords.containsKey("uuid");
    }

    public String getUuid() {
        getKeywords();
        return keywords.get("uuid");
    }

    public boolean hasBricklinkItemNumber() {
        getKeywords();
        return keywords.containsKey("bl");
    }

    public String getBricklinkItemNumber() {
        getKeywords();
        return keywords.get("bl");
    }

    public Map<String, String> getKeywords() {
        if (null == keywords) {
            try {
                ImageMetadata m = imageCollector.getImageMetadata()
                                                .apply(path.toUri()
                                                           .toURL());
                Stream<ImageMetadataItem> items = imageCollector.getJpgImageMetadataItems()
                                                                .apply(m);
                keywords = items.filter(imageCollector.getKeywordsFilter())
                                .flatMap(imageCollector.getKeywordsExtractor())
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k1));
            } catch (MalformedURLException e) {
                throw new LegoImagingException(e);
            }
        }
        return keywords;
    }
}
