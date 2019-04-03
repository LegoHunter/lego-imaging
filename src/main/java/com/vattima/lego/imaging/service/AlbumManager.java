package com.vattima.lego.imaging.service;

import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.ImageFileHolder;

public interface AlbumManager {
    AlbumManifest addPhoto(ImageFileHolder imageFileHolder);
    AlbumManifest movePhoto(ImageFileHolder imageFileHolder);
    AlbumManifest uploadToPhotoService(ImageFileHolder imageFileHolder, AlbumManifest albumManifest);
}
