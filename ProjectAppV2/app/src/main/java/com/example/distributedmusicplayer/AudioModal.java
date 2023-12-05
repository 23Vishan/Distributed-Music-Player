package com.example.distributedmusicplayer;

public class AudioModal {
    private String title;
    private String artist;
    private String time;
    private byte[] image;

    public AudioModal(String title, String artist, String time, byte[] image) {
        this.title = title;
        this.artist = artist;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
