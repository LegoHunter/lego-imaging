package com.vattima.lego.imaging.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public interface PhotoSource {
    InputStream inputStream() throws IOException;
    URI uri() throws URISyntaxException;
    PhotoSource move(final URI uri) throws IOException;
}
