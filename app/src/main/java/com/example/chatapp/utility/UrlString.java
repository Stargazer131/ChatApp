package com.example.chatapp.utility;

import androidx.annotation.NonNull;

public class UrlString {
    private String url;
    private int start;
    private int end;

    public UrlString(String url, int start, int end) {
        this.url = url;
        this.start = start;
        this.end = end;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @NonNull
    @Override
    public String toString() {
        return "UrlString{" +
                "url='" + url + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
