package io.legohunter.imaging.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ProcessedPhoto {
    private String md5;
    private Map<String, String> keywords;
}