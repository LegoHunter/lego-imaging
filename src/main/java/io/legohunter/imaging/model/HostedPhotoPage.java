package io.legohunter.imaging.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class HostedPhotoPage {
    @Singular
    private List<HostedPhoto> photos;
    private Integer page;
    private Integer pages;
    private Integer perPage;
    private Integer total;
}
