package io.legohunter.imaging.bitly.model.bitly;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Pagination {
    private String next;

    @JsonProperty("search_after")
    private String searchAfter;

    private Integer size;
}
