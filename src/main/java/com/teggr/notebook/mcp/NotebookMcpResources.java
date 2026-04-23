package com.teggr.notebook.mcp;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.teggr.notebook.model.NoteListItem;
import com.teggr.notebook.service.NoteService;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema;

@Component
public class NotebookMcpResources {

    private final NoteService noteService;

    public NotebookMcpResources(NoteService noteService) {
        this.noteService = noteService;
    }

    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> notes() {

        List<SyncResourceSpecification> resourceSpecifications = new ArrayList<>();

        for (NoteListItem item : noteService.listNotes()) {

            McpSchema.Resource systemInfoResource = new McpSchema.Resource(
                    "note://notes/" + item.getId(),
                    item.getTitle(),
                    item.getTitle(),
                    "Note content",
                    null,
                    null,
                    null, null);

            McpServerFeatures.SyncResourceSpecification resourceSpecification = new McpServerFeatures.SyncResourceSpecification(
                    systemInfoResource, (exchange, request) -> {

                        return new McpSchema.ReadResourceResult(
                                List.of(
                                        new McpSchema.TextResourceContents(
                                                "note://notes/" + item.getId(),
                                                "text/markdown",
                                                noteService.getNote(item.getId()).get().getContent())));

                    });

            resourceSpecifications.add(resourceSpecification);

        }

        return resourceSpecifications;

    }

    @McpResource(uri = "note://notes/{id}", name = "Note", title = "Note", description = "Content of a note")
    public String noteResource(String id) {
        return noteService.getNote(id).get().getContent();
    }

}
