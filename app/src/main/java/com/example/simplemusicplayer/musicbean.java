package com.example.simplemusicplayer;

public class musicbean {

    private String id;//歌曲id
    private String song;//歌名
    private String singer;//歌手名
    private String album;//歌曲专辑
    private String time;//歌曲时长
    private String path;//歌曲路径


    public musicbean() {
    }

    public musicbean(String id, String song, String singer, String album, String time, String path) {
        this.id = id;
        this.song = song;
        this.singer = singer;
        this.album = album;
        this.time = time;
        this.path = path;

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
