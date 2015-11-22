package com.fefeyo.matsuri;

/**
 * Created by enkaism on 11/22/15.
 */
public class Comment {
    private int vpos;
    private String comment;

    public Comment(int vpos, String comment) {
        this.vpos = vpos;
        this.comment = comment;
    }

    public int getVpos() {
        return vpos;
    }

    public void setVpos(int vpos) {
        this.vpos = vpos;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
