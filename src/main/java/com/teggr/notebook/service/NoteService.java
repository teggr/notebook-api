package com.teggr.notebook.service;

import com.teggr.notebook.config.NotebookProperties;
import com.teggr.notebook.model.Note;
import com.teggr.notebook.model.NoteListItem;
import com.teggr.notebook.model.NoteSearchItem;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final Path notesDir;
    private final GitService gitService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());

    public NoteService(NotebookProperties props, GitService gitService) {
        this.notesDir = Path.of(props.getNotesDir());
        this.gitService = gitService;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(notesDir);
        Files.createDirectories(notesDir.resolve("images"));
        gitService.initIfNeeded(notesDir);
    }

    public List<NoteListItem> listNotes() {
        try {
            return Files.list(notesDir)
                    .filter(p -> p.toString().endsWith(".md"))
                    .sorted((a, b) -> {
                        try {
                            return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                        } catch (IOException e) { return 0; }
                    })
                    .map(p -> {
                        try {
                            String content = Files.readString(p);
                            String title = extractTitle(content);
                            String id = toId(p.getFileName().toString().replace(".md", ""));
                            Instant lastMod = Files.getLastModifiedTime(p).toInstant();
                            return new NoteListItem(id, title, FORMATTER.format(lastMod));
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public Optional<Note> getNote(String id) {
        Path file = notesDir.resolve(decodeId(id) + ".md");
        if (!Files.exists(file)) return Optional.empty();
        try {
            String content = Files.readString(file);
            String title = extractTitle(content);
            Instant lastMod = Files.getLastModifiedTime(file).toInstant();
            return Optional.of(new Note(id, title, content, lastMod));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public List<NoteSearchItem> searchNotes(String query, int limit) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isEmpty()) {
            return Collections.emptyList();
        }

        int safeLimit = Math.max(1, Math.min(limit, 10));

        try {
            return Files.list(notesDir)
                    .filter(p -> p.toString().endsWith(".md"))
                    .map(p -> toSearchCandidate(p, normalizedQuery))
                    .filter(Objects::nonNull)
                    .sorted(Comparator
                            .comparing(SearchCandidate::isTitleMatch).reversed()
                            .thenComparing(SearchCandidate::lastModified, Comparator.reverseOrder()))
                    .limit(safeLimit)
                    .map(c -> c.item)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public Note createNote(String title, String content) throws IOException {
        String id = URLEncoder.encode(UUID.randomUUID().toString(), StandardCharsets.UTF_8);
        Path file = notesDir.resolve(decodeId(id) + ".md");
        Files.writeString(file, content);
        Instant lastMod = Files.getLastModifiedTime(file).toInstant();
        gitService.commitAsync("Create: " + title, notesDir);
        return new Note(id, title, content, lastMod);
    }

    public Note updateNote(String id, String content) throws IOException {
        Path file = notesDir.resolve(decodeId(id) + ".md");
        Files.writeString(file, content);
        String title = extractTitle(content);
        Instant lastMod = Files.getLastModifiedTime(file).toInstant();
        gitService.commitAsync("Update: " + title, notesDir);
        return new Note(id, title, content, lastMod);
    }

    public void deleteNote(String id) throws IOException {
        Path file = notesDir.resolve(decodeId(id) + ".md");
        Files.deleteIfExists(file);
    }

    public String extractTitle(String content) {
        if (content == null || content.isBlank()) return "Untitled";
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("# ")) return line.substring(2).trim();
            if (!line.isEmpty()) return line;
        }
        return "Untitled";
    }

    public String toId(String title) {
        String sanitized = title.replaceAll("[^a-zA-Z0-9 _-]", "").trim().replaceAll("\\s+", "-");
        if (sanitized.isEmpty()) sanitized = UUID.randomUUID().toString();
        return URLEncoder.encode(sanitized, StandardCharsets.UTF_8);
    }

    private String decodeId(String id) {
        return URLDecoder.decode(id, StandardCharsets.UTF_8);
    }

    public Path getNotesDir() {
        return notesDir;
    }

    private SearchCandidate toSearchCandidate(Path file, String query) {
        try {
            String content = Files.readString(file);
            String title = extractTitle(content);
            String titleLower = title.toLowerCase(Locale.ROOT);
            String contentLower = content.toLowerCase(Locale.ROOT);

            boolean titleMatch = titleLower.contains(query);
            boolean contentMatch = contentLower.contains(query);
            if (!titleMatch && !contentMatch) {
                return null;
            }

            String fileStem = file.getFileName().toString().replace(".md", "");
            String id = toId(fileStem);
            Instant lastMod = Files.getLastModifiedTime(file).toInstant();
            String snippet = buildSnippet(content, query);

            NoteSearchItem item = new NoteSearchItem(id, title, snippet, FORMATTER.format(lastMod));
            return new SearchCandidate(item, titleMatch, lastMod);
        } catch (IOException e) {
            return null;
        }
    }

    private String buildSnippet(String content, String query) {
        if (content == null || content.isBlank()) {
            return "Untitled";
        }

        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return "Untitled";
        }

        String normalizedLower = normalized.toLowerCase(Locale.ROOT);
        int contentMatchIndex = normalizedLower.indexOf(query.toLowerCase(Locale.ROOT));

        if (contentMatchIndex < 0) {
            return truncateSnippet(normalized, 120);
        }

        int start = Math.max(0, contentMatchIndex - 30);
        int end = Math.min(normalized.length(), contentMatchIndex + 90);
        String snippet = normalized.substring(start, end).trim();

        if (start > 0) {
            snippet = "..." + snippet;
        }
        if (end < normalized.length()) {
            snippet = snippet + "...";
        }
        return snippet;
    }

    private String truncateSnippet(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3).trim() + "...";
    }

    private record SearchCandidate(NoteSearchItem item, boolean isTitleMatch, Instant lastModified) {}
}
