package ru.seva.lab2.analyzers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SequentialAnalyzer implements WordAnalyzer {

    @Override
    public Map<String, Long> analyze(Path directory, int threadCount) throws IOException {
        Map<String, Long> frequencies = new HashMap<>();

        // Получаем список текстовых файлов, сортируем их для детерминированности
        List<Path> textFiles;
        try (var paths = Files.list(directory)) {
            textFiles = paths
                    .filter(p -> p.toString().endsWith(".txt") && p.getFileName().toString().startsWith("text-"))
                    .sorted()
                    .toList();
        }

        for (Path file : textFiles) {
            processFile(file, frequencies);
        }

        return frequencies;
    }

    private void processFile(Path file, Map<String, Long> frequencies) throws IOException {
        // Читаем файл построчно для экономии памяти
        try (var lines = Files.lines(file)) {
            lines.forEach(line -> {
                String[] words = line.split("\\s+");
                for (String rawWord : words) {
                    String normalized = normalize(rawWord);
                    if (!normalized.isEmpty()) {
                        frequencies.merge(normalized, 1L, Long::sum);
                    }
                }
            });
        }
    }

    private String normalize(String word) {
        return word.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    @Override
    public String getVariantName() {
        return "sequential";
    }
}