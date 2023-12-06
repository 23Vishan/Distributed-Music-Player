package org.example;

import java.io.Serializable;

public class MusicData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;
    private String artist;
    private String musicPath;
    private String time;
    private byte[] image;

    // Constructors, getters, and setters

    public MusicData(String title, String artist, String musicPath, String time, byte[] image) {
        this.title = title;
        this.artist = artist;
        this.musicPath = musicPath;
        this.time = time;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImage() {
        return image;
    }

    public void setTime(byte[] image) {
        this.image = image;
    }
}
