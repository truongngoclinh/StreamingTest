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

    public int getIsArchiving() {
        return isArchiving;
    }

    public void setIsArchiving(int isArchiving) {
        this.isArchiving = isArchiving;
    }

    public int getIsMakeArhieve() {
        return isMakeArhieve;
    }

    public void setIsMakeArhieve(int isMakeArhieve) {
        this.isMakeArhieve = isMakeArhieve;
    }

    public int getIsLiveChat() {
        return isLiveChat;
    }

    public void setIsLiveChat(int isLiveChat) {
        this.isLiveChat = isLiveChat;
    }

    public int getRestriction() {
        return restriction;
    }

    public void setRestriction(int restriction) {
        this.restriction = restriction;
    }

    private int isArchiving;
    private int isMakeArhieve;
    private int isLiveChat;
    private int restriction;


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
