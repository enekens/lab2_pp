package ru.seva.lab2.analyzers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapGetPutAnalyzer extends AbstractConcurrentAnalyzer {

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
                        // Ошибка: операция не является атомарной
                        Long current = frequencies.getOrDefault(normalized, 0L);
                        frequencies.put(normalized, current + 1);
                    }
                }
            });
        }
    }

    @Override
    public String getVariantName() {
        return "concurrent-get-put";
    }
}