package com.vattima.lego.imaging.model;

import com.vattima.lego.imaging.LegoImagingException;
import com.vattima.lego.imaging.file.ImageCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import net.bricklink.data.lego.dto.BricklinkInventory;
import org.apache.commons.imaging.common.ImageMetadata;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;

@RequiredArgsConstructor
@Slf4j
public class ImageFileHolder {
    private final Path path;
    private final ImageCollector imageCollector;
    private final BricklinkInventoryDao bricklinkInventoryDao;

    private Map<String, String> keywords;

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

    public void updateInventoryFromKeywords() {
        BricklinkInventory bricklinkInventory = buildBricklinkInventoryFromKeywords(getKeywords());
        bricklinkInventoryDao.updateFromImageKeywords(bricklinkInventory);
    }

    public BricklinkInventory buildBricklinkInventoryFromKeywords(final Map<String, String> keywords) {
        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        Optional.ofNullable(keywords.get("uuid"))
                .ifPresent(bricklinkInventory::setUuid);
        Optional.ofNullable(keywords.get("bl"))
                .ifPresent(bricklinkInventory::setBlItemNo);
        Optional.ofNullable(keywords.get("sealed"))
                .ifPresent(v -> bricklinkInventory.setSealed(Boolean.valueOf(v)));
        Optional.ofNullable(keywords.get("bc"))
                .ifPresent(bricklinkInventory::setBoxConditionCode);
        Optional.ofNullable(keywords.get("ic"))
                .ifPresent(bricklinkInventory::setInstructionsConditionCode);
        return bricklinkInventory;
    }
}
