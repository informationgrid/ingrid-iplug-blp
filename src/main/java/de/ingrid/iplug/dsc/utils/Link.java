package de.ingrid.iplug.dsc.utils;

public class Link {
    
    public Link(String url, String text) {
        this.url = url;
        this.text = text;
    }

    private String url;
    private String text;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
