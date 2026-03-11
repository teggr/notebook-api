package com.teggr.notebook.model;

public class NoteSearchItem {
    private String id;
    private String title;
    private String snippet;
    private String lastModifiedFormatted;

    public NoteSearchItem(String id, String title, String snippet, String lastModifiedFormatted) {
        this.id = id;
        this.title = title;
        this.snippet = snippet;
        this.lastModifiedFormatted = lastModifiedFormatted;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

    public String getLastModifiedFormatted() {
        return lastModifiedFormatted;
    }
}
