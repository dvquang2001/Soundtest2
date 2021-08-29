package com.example.model;

public class Audio {
    private int music_file;
    private int pic_file;
    private String key;


    public Audio(String key,int music_file, int pic_file) {
        this.key = key;
        this.music_file = music_file;
        this.pic_file = pic_file;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getMusic_file() {
        return music_file;
    }

    public void setMusic_file(int music_file) {
        this.music_file = music_file;
    }

    public int getPic_file() {
        return pic_file;
    }

    public void setPic_file(int pic_file) {
        this.pic_file = pic_file;
    }
}
