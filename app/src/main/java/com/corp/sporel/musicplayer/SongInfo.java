package com.corp.sporel.musicplayer;

import android.graphics.Bitmap;

/**
 * Created by sporel on 12/22/15.
 */
public class SongInfo {

    public Long getAlbumId() {
        return albumId;
    }

    public Bitmap getAlbumArt() {
        return albumArt;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public void setAlbumArt(Bitmap albumArt) {
        this.albumArt = albumArt;
    }

    private Long albumId;
    private Bitmap albumArt;
}
