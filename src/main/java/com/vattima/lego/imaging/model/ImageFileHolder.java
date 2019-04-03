package com.vattima.lego.imaging.model;

import com.vattima.lego.imaging.LegoImagingException;
import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.file.ImageCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import net.bricklink.data.lego.dto.BricklinkInventory;
import org.apache.commons.imaging.common.ImageMetadata;
import org.springframework.util.StopWatch;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
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
    private final LegoImagingProperties legoImagingProperties;

    private Map<String, String> keywords;
    private String md5Hash;
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();


    public Path getPath() {
        return path;
    }

    public String getMd5Hash() {
        return Optional.ofNullable(md5Hash).orElseGet(this::computeMd5Hash);
    }

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

    public Path getTargetDirectory() {
        return Paths.get(legoImagingProperties.getRootInventoryItemsFolder() + "/" + getBricklinkItemNumber() + "-" + getUuid());
    }

    public void updateInventoryFromKeywords() {
        BricklinkInventory bricklinkInventory = buildBricklinkInventoryFromKeywords(getKeywords());
        bricklinkInventoryDao.updateFromImageKeywords(bricklinkInventory);
    }

    public boolean canMove() {
        return (hasUuid() && hasBricklinkItemNumber());
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

    public PhotoMetaData getPhotoMetaData() {
        PhotoMetaData photoMetaData = new PhotoMetaData();
        photoMetaData.setUploadReturnCode(-1);
        photoMetaData.setPrimary(keywords.containsKey("primary"));
        photoMetaData.setPath(this.getPath());
        photoMetaData.setMd5(getMd5Hash());
        return photoMetaData;
    }

    public String computeMd5Hash() {
        StopWatch timer = new StopWatch();
        timer.start();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            InputStream is = Files.newInputStream(path);
            BufferedInputStream bis = new BufferedInputStream(is);
            DigestInputStream dis = new DigestInputStream(bis, md);
            byte[] bytes = new byte[8192];
            while ((dis.read(bytes)) != -1);
        } catch (Exception e) {
            throw new LegoImagingException(e);
        }
        byte[] digest = Optional.ofNullable(md)
                                .map(MessageDigest::digest)
                                .orElseThrow(() -> new LegoImagingException("Unable to compute MD5 hash for file [" + path.toFile()
                                                                                                                          .getAbsolutePath() + "]"));
        this.md5Hash = bytesToHex(digest);
        timer.stop();
        log.info("computed digest [{}] for file [{}] in [{}] ms", this.md5Hash, path.toFile().getAbsolutePath(), timer.getTotalTimeMillis());
        return md5Hash;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
