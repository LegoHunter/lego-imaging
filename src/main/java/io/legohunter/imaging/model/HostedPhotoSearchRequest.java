package io.legohunter.imaging.model;

import lombok.Builder;
import lombok.Data;

/** One page of an account-wide photo listing. */
@Data
@Builder
public class HostedPhotoSearchRequest {
    private Integer page;
    private Integer perPage;
}
