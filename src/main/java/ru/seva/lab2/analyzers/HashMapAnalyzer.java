package ru.seva.lab2.analyzers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HashMapAnalyzer extends AbstractConcurrentAnalyzer {

    @Override
    protected Map<String, Long> createMap() {
        return new HashMap<>();
    }

    @Override
    protected void processFile(Path file, Map<String, Long> frequencies) throws IOException {
        try (var lines = Files.lines(file)) {
            lines.forEach(line -> {
                for (String rawWord : line.split("\\s+")) {
                    String normalized = normalize(rawWord);
                    if (!normalized.isEmpty()) {
                        frequencies.merge(normalized, 1L, Long::sum);
                    }
                }
            });
        }
    }

    @Override
    public String getVariantName() {
        return "hashmap";
    }
}