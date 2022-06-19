package kr.co.company.hw3;

import android.graphics.Bitmap;
import android.net.Uri;

public class Music {
    private final long id;


    private final String path;
    private final Uri uri;
    private final String title;
    private final int duration;
    private final int size;
    private final Bitmap albumArt;

    public Uri getUri() {
        return uri;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public long getId() {
        return id;
    }

    public int getDuration() {
        return duration;
    }

    public int getSize() {
        return size;
    }

    public Bitmap getAlbumArt() {
        return albumArt;
    }

    public Music(long id, String path, Uri uri, String title, int duration, int size, Bitmap albumArt) {
        this.id = id;
        this.path = path;
        this.uri = uri;
        this.title = title;
        this.duration = duration;
        this.size = size;
        this.albumArt = albumArt;
    }
}
