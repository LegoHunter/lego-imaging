package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.LegoImagingException;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.ImageManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class ImageManagerImpl implements ImageManager {

    @Override
    public Map<String, String> getKeywords(final PhotoMetaData photoMetaData, final String keywordsName) {
        return Optional
                .ofNullable(photoMetaData.getKeywords())
                .filter(m -> !m.isEmpty())
                .orElseGet(() -> {
                    try {
                        Map<String, String> keywords = Imaging
                                .getMetadata(photoMetaData.getAbsolutePath()
                                                          .toFile())
                                .getItems()
                                .stream()
                                .filter(m -> Optional.of(m.toString())
                                                     .map(s -> s.startsWith(keywordsName))
                                                     .orElse(false))
                                .peek(m -> {
                                    log.debug("[{}]", m.toString());
                                })
                                .flatMap(m -> KeywordsSplitter.of(keywordsName, m.toString())
                                                              .map(KeywordsSplitter.tokenizer))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k1));
                        photoMetaData.setKeywords(keywords);
                        return photoMetaData.getKeywords();
                    } catch (ImageReadException | IOException e) {
                        throw new LegoImagingException(e);
                    }
                });
    }

    static class KeywordsSplitter {
        private static String pairDelimiter = "[;,\\s]";

        static Stream<String> of(String keywordsKeyName, String keywordString, String pairDelimiter) {
            final Pattern pattern = Pattern.compile("^" + keywordsKeyName + "\\s*'?(.*?)'?$");
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
            String[] tokens = s.split("[:=]");
            return new AbstractMap.SimpleEntry<>(tokens[0], (s.length() >= tokens[0].length() + 1) ? s.substring(tokens[0].length() + 1) : tokens[0]);
        };
    }
}
