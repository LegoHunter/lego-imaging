package io.legohunter.imaging.bitly.model.bitly;

import lombok.Data;

import java.util.List;

@Data
public class BitlinksPage {
    private List<Bitlink> links;
    private Pagination pagination;
}
