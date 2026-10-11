package ru.seva.lab2.analyzers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.LongAdder;

public class LongAdderAnalyzer implements WordAnalyzer {

    @Override
    public Map<String, Long> analyze(Path directory, int threadCount) throws IOException, InterruptedException, ExecutionException {
        ConcurrentHashMap<String, LongAdder> frequencies = new ConcurrentHashMap<>();

        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount)) {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<?>>();

            for (Path file : getFiles(directory)) {
                futures.add(executor.submit(() -> {
                    try (var lines = Files.lines(file)) {
                        lines.forEach(line -> {
                            for (String rawWord : line.split("\\s+")) {
                                String normalized = normalize(rawWord);
                                if (!normalized.isEmpty()) {
                                    frequencies.computeIfAbsent(normalized, k -> new LongAdder()).increment();
                                }
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            for (var future : futures) {
                future.get();
            }
        }

        // Конвертируем LongAdder в обычные Long для итогового отчета
        Map<String, Long> result = new HashMap<>();
        frequencies.forEach((word, adder) -> result.put(word, adder.sum()));
        return result;
    }

    // Вспомогательный метод для получения файлов (дублируем из базового класса для независимости)
    private java.util.List<Path> getFiles(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            return paths
                    .filter(p -> p.toString().endsWith(".txt") && p.getFileName().toString().startsWith("text-"))
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    private String normalize(String word) {
        return word.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    @Override
    public String getVariantName() {
        return "long-adder";
    }
}