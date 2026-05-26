package io.legohunter.imaging.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class HostedAlbumPage {
    @Singular
    private List<HostedAlbum> albums;
    private Integer page;
    private Integer pages;
    private Integer perPage;
    private Integer total;
}
