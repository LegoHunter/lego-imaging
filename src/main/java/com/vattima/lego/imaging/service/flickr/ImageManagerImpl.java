package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.ImageManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;

@Slf4j
@Component
public class ImageManagerImpl implements ImageManager {

    private List<MetaDataExtractor> extractors = new ArrayList<>();
    private Map<String, String> keywords = new ConcurrentHashMap<>();

    public ImageManagerImpl() {
        extractors.add(build("Keywords:",
                (mdi, v) -> mdi.toString()
                               .startsWith(v.get()),
                mdi -> Arrays.stream(mdi.toString()
                                    .split("[;,\\s]")),
                s -> {
                    String[] tokens = s.split("[:=]");
                    return new AbstractMap.SimpleEntry<>(tokens[0], (s.length() >= tokens[0].length() + 1) ? s.substring(tokens[0].length() + 1) : tokens[0]);
                }));
        extractors.add(build("Caption/Abstract:",
                (mdi, v) -> mdi.toString()
                               .startsWith(v.get()),
                mdi -> Stream.of(mdi.toString()),
                s -> new AbstractMap.SimpleEntry<>("cp", s)));
    }

    private Stream<MetaDataExtractor> extractors() {
        return extractors.stream();
    }

    @Override
    public Map<String, String> getKeywords(final PhotoMetaData photoMetaData) {
        ImageMetadata imageMetadata;
        try {
            imageMetadata = Imaging.getMetadata(photoMetaData.getAbsolutePath()
                                                             .toFile());
        } catch (ImageReadException | IOException e) {
            throw new RuntimeException(e);
        }

        keywords = imageMetadata.getItems()
                                .stream()
                                .flatMap(mdi -> {
                                    Optional<MetaDataExtractor> extractor = extractors().filter(e -> e.filter()
                                                                                                      .test(mdi, e))
                                                                                        .findFirst();
                                    if (!extractor.isPresent()) {
                                        return Stream.empty();
                                    }
                                    MetaDataExtractor e = extractor.get();
                                    Matcher m = e.pattern().matcher(mdi.toString());
                                    Stream<String> s = Stream.empty();
                                    if (m.find()) {
                                        Optional<String> v = Optional.ofNullable(m.group(1));
                                        if (v.isPresent()) {
                                            s = e.splitter().apply(v.get());
                                        }
                                    }
                                    return s.map(x -> e.tokenizer().apply(x));
                                })
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k1));
        photoMetaData.setKeywords(keywords);
        return keywords;
    }

    private MetaDataExtractor build(String metaDataItemName,
                                    BiPredicate<ImageMetadataItem, Supplier<String>> filter,
                                    Function<String, Stream<String>> splitter,
                                    Function<String, AbstractMap.SimpleEntry<String, String>> tokenizer) {
        return new MetaDataExtractor() {
            Pattern pattern = Pattern.compile("^" + metaDataItemName + "\\s*'?(.*?)'?$");
            BiPredicate<ImageMetadataItem, Supplier<String>> _filter = filter;
            Function<String, Stream<String>> _splitter = splitter;
            Function<String, AbstractMap.SimpleEntry<String, String>> _tokenizer = tokenizer;

            @Override
            public String get() {
                return metaDataItemName;
            }

            @Override
            public Pattern pattern() {
                return pattern;
            }

            @Override
            public BiPredicate<ImageMetadataItem, Supplier<String>> filter() {
                return _filter;
            }

            @Override
            public Function<String, Stream<String>> splitter() {
                return _splitter;
            }

            @Override
            public Function<String, AbstractMap.SimpleEntry<String, String>> tokenizer() {
                return _tokenizer;
            }
        };
    }

    private interface MetaDataExtractor extends Supplier<String> {
        Pattern pattern();

        BiPredicate<ImageMetadataItem, Supplier<String>> filter();

        Function<String, Stream<String>> splitter();

        Function<String, AbstractMap.SimpleEntry<String, String>> tokenizer();
    }
}
