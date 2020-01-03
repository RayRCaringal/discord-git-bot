package com.rayrcaringal.discordgitbot.commands;



public class fullRepo {
    private String text;
    private int foldNum;
    private int start;

    public fullRepo(int foldNum, String text){
        this.text = text;
        this.foldNum = foldNum;
        this.start = 0;
    }

    public fullRepo(int foldNum, String text, int start){
        this.text = text;
        this.foldNum = foldNum;
        this.start = start;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getFoldNum() {
        return foldNum;
    }

    public void setFoldNum(int foldNum) {
        this.foldNum = foldNum;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
