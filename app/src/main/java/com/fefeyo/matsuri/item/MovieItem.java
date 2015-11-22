package com.fefeyo.matsuri.item;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by USER on 2015/11/22.
 */
public class MovieItem {
    private String title;
    private String postdate;
    private String thmbnailUrl;
    private String number;
    private String movieUrl;
    private int[] comments;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPostdate() {
        return postdate;
    }

    public void setPostdate(String postdate) {
        this.postdate = postdate;
    }

    public String getThmbnailUrl() {
        return thmbnailUrl;
    }

    public void setThmbnailUrl(String thmbnailUrl) {
        this.thmbnailUrl = thmbnailUrl;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMovieUrl() {
        return movieUrl;
    }

    public void setMovieUrl(String movieUrl) {
        this.movieUrl = movieUrl;
    }

    public int[] getComments() {
        return comments;
    }

    public void setComments(int[] comments) {
        this.comments = comments;
    }
}
