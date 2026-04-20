package com.teggr.notebook.mcp;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.context.MetaProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.teggr.notebook.model.Note;
import com.teggr.notebook.service.NoteService;

@Component
public class NotebookMcpApps {

    private final NoteService noteService;

    @Value("classpath:/app/note-view.html")
    private Resource noteViewAppResource;

    public NotebookMcpApps(NoteService noteService) {
        this.noteService = noteService;
    }

    @McpTool(title = "View the note", name = "view-the-note", 
        description = "Renders the note in HTML for reading", 
        metaProvider = ViewTheNoteMetaProvider.class)
    public Note viewTheNote( String noteId ) {
        return noteService.getNote(noteId).orElseThrow();
    }

    public static final class ViewTheNoteMetaProvider implements MetaProvider {
        @Override
        public Map<String, Object> getMeta() {
            return Map.of("ui",
                    Map.of(
                            "resourceUri", "ui://note/note-view.html"));
        }
    }

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");

    @McpTool(
        title = "Get Note Image",
        name = "get-note-image",
        description = "Returns a note image as base64-encoded data for display inside the note viewer",
        metaProvider = GetNoteImageMetaProvider.class
    )
    public Map<String, String> getNoteImage(String filename) throws IOException {
        if (filename == null || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        String ext = "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0) {
            ext = filename.substring(dotIndex + 1).toLowerCase();
        }
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Unsupported image format: " + ext);
        }
        Path imagesDir = noteService.getNotesDir().resolve("images").normalize();
        Path imageFile = imagesDir.resolve(filename).normalize();
        if (!imageFile.startsWith(imagesDir)) {
            throw new IllegalArgumentException("Invalid image path");
        }
        if (!Files.exists(imageFile)) {
            throw new IllegalArgumentException("Image not found: " + filename);
        }
        byte[] bytes = Files.readAllBytes(imageFile);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String mimeType = switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif"         -> "image/gif";
            case "webp"        -> "image/webp";
            default            -> "image/png";
        };
        return Map.of("base64", base64, "mimeType", mimeType);
    }

    public static final class GetNoteImageMetaProvider implements MetaProvider {
        @Override
        public Map<String, Object> getMeta() {
            return Map.of("ui", Map.of("visibility", List.of("app")));
        }
    }

    @McpResource(name = "Note View App Resource",
        uri = "ui://note/note-view.html",
        mimeType = "text/html;profile=mcp-app",
        metaProvider = CspMetaProvider.class)
    public String getNoteViewAppResource() throws IOException {
        return noteViewAppResource.getContentAsString(Charset.defaultCharset());
    }

    public static final class CspMetaProvider implements MetaProvider {
        @Override
        public Map<String, Object> getMeta() {
            return Map.of("ui",
                    Map.of("csp",
                            Map.of("resourceDomains",
                                    List.of("https://unpkg.com"))));
        }
    }

}
