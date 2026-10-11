package ru.seva.lab2.analyzers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public abstract class AbstractConcurrentAnalyzer implements WordAnalyzer {

    @Override
    public Map<String, Long> analyze(Path directory, int threadCount) throws IOException, InterruptedException, ExecutionException {
        List<Path> files = getFiles(directory);
        Map<String, Long> frequencies = createMap();

        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            List<Future<?>> futures = new ArrayList<>();

            for (Path file : files) {
                futures.add(executor.submit(() -> {
                    try {
                        processFile(file, frequencies);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get(); // Ожидаем завершения каждого потока
            }
        }

        return frequencies;
    }

    protected abstract Map<String, Long> createMap();

    protected abstract void processFile(Path file, Map<String, Long> frequencies) throws IOException;

    protected List<Path> getFiles(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            return paths
                    .filter(p -> p.toString().endsWith(".txt") && p.getFileName().toString().startsWith("text-"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    protected String normalize(String word) {
        return word.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}