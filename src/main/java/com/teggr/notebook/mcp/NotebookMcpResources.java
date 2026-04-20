package com.teggr.notebook.mcp;

import java.util.List;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import com.teggr.notebook.service.NoteService;

@Component
public class NotebookMcpResources {

    private final NoteService noteService;

    public NotebookMcpResources(NoteService noteService) {
        this.noteService = noteService;
    }

    @McpResource(
        uri="note://notes",
        name="Notes",
        title="Notes",
        description = "List of notes",
        mimeType = "application/json"
    )
    public List<String> notes() {
        return noteService.listNotes().stream().map( i -> "note://notes/" + i.getId() ).toList();
    }
    
    @McpResource(
        uri="note://notes/{id}",
        name="Note",
        title="Note",
        description = "Content of a note"
    )
    public String noteResource(String id) {
        return noteService.getNote(id).get().getContent();
    }

}
