package com.teggr.notebook;

import com.teggr.notebook.config.NotebookProperties;
import com.teggr.notebook.controller.NoteApiController;
import com.teggr.notebook.model.Note;
import com.teggr.notebook.model.NoteSearchItem;
import com.teggr.notebook.service.GitService;
import com.teggr.notebook.service.NoteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NoteApiSearchTest {

    @TempDir
    Path tempDir;

    private GitService gitService;
    private NoteApiController controller;

    @BeforeEach
    void setUp() throws IOException {
        NotebookProperties props = new NotebookProperties();
        props.setNotesDir(tempDir.toString());
        gitService = new GitService();
        NoteService noteService = new NoteService(props, gitService);
        noteService.init();
        controller = new NoteApiController(noteService);
    }

    @AfterEach
    void tearDown() {
        gitService.shutdown();
    }

    @Test
    void searchNotesUsesDefaultLimitWhenMissing() throws IOException {
        controller.createNote(java.util.Map.of("title", "Alpha", "content", "# Alpha\n\nquery in content"));

        List<NoteSearchItem> result = controller.searchNotes("query", null);

        assertEquals(1, result.size());
    }

    @Test
    void searchNotesCapsLimitAtTen() throws IOException {
        for (int i = 0; i < 12; i++) {
            Note note = controller.createNote(java.util.Map.of(
                    "title", "Query " + i,
                    "content", "# Query " + i + "\n\nBody"
            )).getBody();
            assertNotNull(note);
        }

        List<NoteSearchItem> result = controller.searchNotes("query", 25);

        assertEquals(10, result.size());
    }

    @Test
    void searchNotesPassesExplicitLimit() throws IOException {
        controller.createNote(java.util.Map.of("title", "One", "content", "# One\n\nquery"));
        controller.createNote(java.util.Map.of("title", "Two", "content", "# Two\n\nquery"));
        controller.createNote(java.util.Map.of("title", "Three", "content", "# Three\n\nquery"));

        List<NoteSearchItem> result = controller.searchNotes("query", 2);

        assertEquals(2, result.size());
    }
}
