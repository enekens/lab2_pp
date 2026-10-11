package ru.seva.lab2.analyzers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapMergeAnalyzer extends AbstractConcurrentAnalyzer {

    @Override
    protected Map<String, Long> createMap() {
        return new ConcurrentHashMap<>();
    }

    @Override
    protected void processFile(Path file, Map<String, Long> frequencies) throws IOException {
        try (var lines = Files.lines(file)) {
            lines.forEach(line -> {
                for (String rawWord : line.split("\\s+")) {
                    String normalized = normalize(rawWord);
                    if (!normalized.isEmpty()) {
                        // Атомарная операция обновления
                        frequencies.merge(normalized, 1L, Long::sum);
                    }
                }
            });
        }
    }

    @Override
    public String getVariantName() {
        return "concurrent-merge";
    }
}