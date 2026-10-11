package ru.seva.lab2.analyzers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class LocalMapsAnalyzer implements WordAnalyzer {

    @Override
    public Map<String, Long> analyze(Path directory, int threadCount) throws IOException, InterruptedException {
        List<Future<Map<String, Long>>> futures = new java.util.ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            for (Path file : getFiles(directory)) {
                futures.add(executor.submit(() -> {
                    Map<String, Long> localMap = new HashMap<>();
                    try (var lines = Files.lines(file)) {
                        lines.forEach(line -> {
                            for (String rawWord : line.split("\\s+")) {
                                String normalized = normalize(rawWord);
                                if (!normalized.isEmpty()) {
                                    localMap.merge(normalized, 1L, Long::sum);
                                }
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return localMap;
                }));
            }
        }

        // Объединяем локальные словари в главном потоке
        Map<String, Long> globalMap = new HashMap<>();
        for (Future<Map<String, Long>> future : futures) {
            try {
                Map<String, Long> localMap = future.get();
                localMap.forEach((word, count) -> globalMap.merge(word, count, Long::sum));
            } catch (ExecutionException e) {
                throw new RuntimeException("Ошибка при получении результата от потока", e);
            }
        }

        return globalMap;
    }

    private List<Path> getFiles(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            return paths
                    .filter(p -> p.toString().endsWith(".txt") && p.getFileName().toString().startsWith("text-"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    private String normalize(String word) {
        return word.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    @Override
    public String getVariantName() {
        return "local-maps";
    }
}