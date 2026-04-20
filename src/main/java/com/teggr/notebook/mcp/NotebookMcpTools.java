package com.teggr.notebook.mcp;

import java.util.List;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import com.teggr.notebook.model.NoteSearchItem;
import com.teggr.notebook.model.NoteSearchResult;
import com.teggr.notebook.service.NoteService;

@Component
public class NotebookMcpTools {

    private final NoteService noteService;
    
    public NotebookMcpTools(NoteService noteService) {
        this.noteService = noteService;
    }

    @McpTool(
        name = "searchNotes", 
        description = "search through the list of user notes", 
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(
            title = "Notes search",
            readOnlyHint = true
        )
    )
    public NoteSearchResult searchNotes(
            @McpToolParam(description="query", required = true) String query,
            @McpToolParam(description = "limit", required = false) Integer limit
    ) {
        int safeLimit = limit == null ? 10 : Math.max(1, Math.min(limit, 10));
        List<NoteSearchItem> items = noteService.searchNotes(query, safeLimit);
        return new NoteSearchResult(items, items.size());
    }

}
