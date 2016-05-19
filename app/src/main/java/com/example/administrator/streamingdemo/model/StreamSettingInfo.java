package com.example.administrator.streamingdemo.model;

import java.io.Serializable;

/**
 * Created by linhtruong on 5/17/2016.
 */
public class StreamSettingInfo implements Serializable {

    private int type;
    private int quanlity;
    private String title;
    private String description;


    private boolean isArchiving;
    private boolean isMakeArhieve;
    private boolean isLiveChat;
    private boolean restriction;

    public boolean isArchiving() {
        return isArchiving;
    }

    public void setArchiving(boolean archiving) {
        isArchiving = archiving;
    }

    public boolean isMakeArhieve() {
        return isMakeArhieve;
    }

    public void setMakeArhieve(boolean makeArhieve) {
        isMakeArhieve = makeArhieve;
    }

    public boolean isLiveChat() {
        return isLiveChat;
    }

    public void setLiveChat(boolean liveChat) {
        isLiveChat = liveChat;
    }

    public boolean isRestriction() {
        return restriction;
    }

    public void setRestriction(boolean restriction) {
        this.restriction = restriction;
    }




    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getQuanlity() {
        return quanlity;
    }

    public void setQuanlity(int quanlity) {
        this.quanlity = quanlity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
