package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.LegoImagingException;
import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.ImageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Component
public class ImageManagerImpl implements ImageManager {
    private final LegoImagingProperties legoImagingProperties;

    @Override
    public String computeMD5Hash(PhotoMetaData photoMetaData) {
        Path path = Optional.ofNullable(photoMetaData)
                            .map(PhotoMetaData::getPath)
                            .filter(p -> Files.exists(p))
                            .orElseThrow(() -> new LegoImagingException("Path does not exist [" + photoMetaData.getPath() + "]"));
        if (null != photoMetaData.getMd5()) {
            return photoMetaData.getMd5();
        }
        String absolutePath = path.toFile()
                                  .getAbsolutePath();
        StopWatch timer = new StopWatch();
        timer.start();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            InputStream is = Files.newInputStream(path);
            BufferedInputStream bis = new BufferedInputStream(is);
            DigestInputStream dis = new DigestInputStream(bis, md);
            byte[] bytes = new byte[8192];
            while ((dis.read(bytes)) != -1) ;
        } catch (Exception e) {
            throw new LegoImagingException(e);
        }
        byte[] digest = Optional.ofNullable(md)
                                .map(MessageDigest::digest)
                                .orElseThrow(() -> new LegoImagingException("Unable to compute MD5 hash for file [" + absolutePath + "]"));
        String md5Hash = bytesToHex(digest);
        timer.stop();
        photoMetaData.setMd5(md5Hash);
        log.info("computed digest [{}] for file [{}] in [{}] ms", md5Hash, absolutePath, timer.getTotalTimeMillis());
        return md5Hash;
    }

    @Override
    public void extractKeywords(PhotoMetaData photoMetaData) {
        Optional.ofNullable(photoMetaData.getKeywords())
                .filter(m -> !m.isEmpty())
                .orElseGet(() -> {
                    try {
                        ImageMetadata m = getImageMetadata()
                                .apply(photoMetaData.getPath()
                                                    .toUri()
                                                    .toURL());
                        Stream<ImageMetadata.ImageMetadataItem> items = getJpgImageMetadataItems()
                                .apply(m);
                        Map<String, String> kw = items.filter(getKeywordsFilter())
                                                      .flatMap(getKeywordsExtractor())
                                                      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k1));
                        photoMetaData.setKeywords(kw);
                        return kw;
                    } catch (MalformedURLException e) {
                        throw new LegoImagingException(e);
                    }
                });
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public Function<URL, ImageMetadata> getImageMetadata() {
        return this.getImageMetadata;
    }

    public Function<ImageMetadata, Stream<ImageMetadata.ImageMetadataItem>> getJpgImageMetadataItems() {
        return jpgImageMetadataItems;
    }

    public Predicate<ImageMetadata.ImageMetadataItem> getKeywordsFilter() {
        return keywordsFilter;
    }

    public Function<ImageMetadata.ImageMetadataItem, Stream<Map.Entry<String, String>>> getKeywordsExtractor() {
        return keywordsExtractor;
    }

    Function<URL, ImageMetadata> getImageMetadata = u -> {
        try {
            return Imaging.getMetadata(new File(u.toURI()));
        } catch (ImageReadException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    };

    Function<ImageMetadata, Stream<ImageMetadata.ImageMetadataItem>> jpgImageMetadataItems = m -> {
        if (m instanceof JpegImageMetadata) {
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) m;
            return jpegMetadata.getItems()
                               .stream();
        } else {
            return Stream.empty();
        }
    };

    Predicate<ImageMetadata.ImageMetadataItem> keywordsFilter = m -> Optional.of(m.toString())
                                                                             .map(s -> s.startsWith(legoImagingProperties.getKeywordsKeyName()))
                                                                             .orElse(false);

    Function<ImageMetadata.ImageMetadataItem, Stream<Map.Entry<String, String>>> keywordsExtractor = m -> Keywords.of(legoImagingProperties.getKeywordsKeyName(), m.toString())
                                                                                                                  .map(Keywords.tokenizer);

    static class Keywords {
        private static String pairDelimiter = "[;,\\s]";

        static Stream<String> of(String keywordsKeyName, String keywordString, String pairDelimiter) {
            final Pattern pattern = Pattern.compile("^" + keywordsKeyName + "\\s+?'?(.*?)'?$");
            Matcher matcher = pattern.matcher(keywordString);
            if (matcher.find()) {
                return Arrays.stream(matcher.group(1)
                                            .split(pairDelimiter));
            } else {
                return Stream.empty();
            }
        }

        static Stream<String> of(String keywordsKeyName, String keywordString) {
            return of(keywordsKeyName, keywordString, pairDelimiter);
        }

        static Function<String, AbstractMap.SimpleEntry<String, String>> tokenizer = s -> {
            String[] tokens = new String[]{"", ""};
            if (s.contains(":")) {
                tokens = s.split(":");
            } else if (s.contains("=")) {
                tokens = s.split("=");
            } else {
                tokens[0] = s;
                tokens[1] = s;
            }
            return new AbstractMap.SimpleEntry<>(tokens[0], (tokens.length == 2) ? tokens[1] : "");
        };
    }
}
