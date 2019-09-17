package com.beviswang.customcontrols.source.model;

import android.support.v4.media.MediaMetadataCompat;

/**
 * 音乐模型，提供音乐参数
 * 包含音乐的
 * 编号id
 * 标题title
 * 歌手artist
 * 专辑album
 * 图片地址imageUrl
 * 时长duration
 * 类型genre
 * 大小size
 * 路径url
 * 比特率 bit
 * 添加日期date
 * Created by shize on 2017/3/28.
 */

public class MusicModel {

    public static final short MODEL_KEY_TITLE = 0x11;
    public static final short MODEL_KEY_ARTIST = 0x12;
    public static final short MODEL_KEY_ALBUM = 0x13;
    public static final short MODEL_KEY_URL = 0x18;

    private long size;
    private String bit;

    private MediaMetadataCompat mediaMetadataCompat; // 元数据

    public MusicModel() {

    }

    public MusicModel(MediaMetadataCompat mediaMetadataCompat) {
        this.mediaMetadataCompat = mediaMetadataCompat;
    }

    /**
     * 临时创建用于缓存专辑图片用缓存
     */
    public MusicModel(String id, String album, String url) {
        mediaMetadataCompat = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, url).build();
    }

    public MusicModel(MediaMetadataCompat mediaMetadataCompat, long size, String bit) {
        this.size = size;
        this.bit = bit;
        this.mediaMetadataCompat = mediaMetadataCompat;
    }

    public MusicModel(String id, String title, String artist, String album, String imageUrl,
                      long duration, String genre, long size, String url, String bit, String date) {
        this(new MediaMetadataCompat.Builder().putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, imageUrl)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, url)
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, date).build(), size, bit);

    }

    public String getId() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
    }

    public String getTitle() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
    }

    public String getArtist() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
    }

    public String getAlbum() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
    }

    public String getImageUrl() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
    }

    public long getDuration() {
        return mediaMetadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
    }

    public String getGenre() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
    }

    public long getSize() {
        return size;
    }

    public String getUrl() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
    }

    public String getBit() {
        return bit;
    }

    public String getDate() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_DATE);
    }

    public MediaMetadataCompat getMediaMetadataCompat() {
        return mediaMetadataCompat;
    }

    public String getString(short key) {
        switch (key) {
            case MODEL_KEY_TITLE:
                return getTitle();
            case MODEL_KEY_ALBUM:
                return getAlbum();
            case MODEL_KEY_ARTIST:
                return getArtist();
            case MODEL_KEY_URL:
                return getUrl();
            default:
                return getId();
        }
    }

    public static class MusicBuilder {

        private String id;
        private String title;
        private String artist;
        private String album;
        private String imageUrl;
        private long duration;
        private String genre;
        private long size;
        private String url;
        private String bit;
        private String date;

        public MusicBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public MusicBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public MusicBuilder setArtist(String artist) {
            this.artist = artist;
            return this;
        }

        public MusicBuilder setAlbum(String album) {
            this.album = album;
            return this;
        }

        public MusicBuilder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public MusicBuilder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public MusicBuilder setGenre(String genre) {
            this.genre = genre;
            return this;
        }

        public MusicBuilder setSize(long size) {
            this.size = size;
            return this;
        }

        public MusicBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        public MusicBuilder setBit(String bit) {
            this.bit = bit;
            return this;
        }

        public MusicBuilder setDate(String date) {
            this.date = date;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public String getAlbum() {
            return album;
        }

        public String getTitle() {
            return title;
        }

        public MusicModel build() {
            return new MusicModel(id, title, artist, album, imageUrl, duration, genre, size,
                    url, bit, date);
        }
    }
}
