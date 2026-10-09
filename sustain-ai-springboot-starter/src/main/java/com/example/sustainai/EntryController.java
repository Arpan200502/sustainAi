package com.example.sustainai;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entries")
public class EntryController {

    private final JdbcTemplate jdbc;

    public EntryController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Save the form data in PostgreSQL.
    @PostMapping
    public Map<String, Object> saveEntry(@RequestBody EntryRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (request.message() == null || request.message().isBlank()) {
            throw new IllegalArgumentException("Message is required.");
        }

        jdbc.update(
            "INSERT INTO entries (name, message) VALUES (?, ?)",
            request.name().trim(),
            request.message().trim()
        );

        return Map.of("message", "Saved to PostgreSQL successfully.");
    }

    // Fetch saved entries so you can see that persistence works.
    @GetMapping
    public List<Map<String, Object>> getEntries() {
        return jdbc.queryForList(
            "SELECT id, name, message, created_at FROM entries ORDER BY id DESC"
        );
    }

    public record EntryRequest(String name, String message) {}
}
