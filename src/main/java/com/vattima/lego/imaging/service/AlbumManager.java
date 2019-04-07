package com.vattima.lego.imaging.service;

import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.PhotoMetaData;

public interface AlbumManager {
    AlbumManifest addPhoto(PhotoMetaData photoMetaData);

    AlbumManifest movePhoto(PhotoMetaData photoMetaData);

    AlbumManifest uploadToPhotoService(PhotoMetaData photoMetaData, AlbumManifest albumManifest);
}
