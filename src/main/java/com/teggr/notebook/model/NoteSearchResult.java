package com.teggr.notebook.model;

import java.util.List;

public class NoteSearchResult {
    private final List<NoteSearchItem> items;
    private final int total;

    public NoteSearchResult(List<NoteSearchItem> items, int total) {
        this.items = items;
        this.total = total;
    }

    public List<NoteSearchItem> getItems() {
        return items;
    }

    public int getTotal() {
        return total;
    }
}
