package com.vattima.lego.imaging.file;

import com.vattima.lego.imaging.LegoImagingException;
import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.model.ImageFileHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@RequiredArgsConstructor
public class ImageHandler {
    private final Path path;
    private final LegoImagingProperties legoImagingProperties;

    public void move(ImageFileHolder imageFileHolder) {
        Path targetPath = null; //legoImagingProperties.getRootInventoryItemsFolder() + "/" +
        try {
            Files.move(path, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            throw new LegoImagingException(e);
        }
    }
}
