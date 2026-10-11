package ru.seva.lab2;

import ru.seva.lab2.analyzers.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.Locale;

public class Benchmark {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        Path directory = Paths.get("generated-data");
        Path expectedFile = directory.resolve("expected-statistics.txt");

        Path resultsDir = Paths.get("results");
        Files.createDirectories(resultsDir);
        Path csvFile = resultsDir.resolve("measurements.csv");

        int[] threadCounts = {1, 2, 4, 8, 16, 32};
        WordAnalyzer[] analyzers = {
                new SequentialAnalyzer(),
                new SynchronizedMapAnalyzer(),
                new ConcurrentHashMapGetPutAnalyzer(),
                new ConcurrentHashMapMergeAnalyzer(),
                new LongAdderAnalyzer(),
                new LocalMapsAnalyzer()
        };

        int warmupRuns = 2;
        int measurementRuns = 3;

        try (BufferedWriter writer = Files.newBufferedWriter(csvFile)) {
            writer.write("variant,threads,time_seconds,correct\n");

            for (WordAnalyzer analyzer : analyzers) {
                int[] threadsToTest = (analyzer instanceof SequentialAnalyzer) ? new int[]{1} : threadCounts;

                for (int threads : threadsToTest) {
                    System.out.printf("Тестирование: %-22s | Потоков: %2d%n", analyzer.getVariantName(), threads);

                    // 1. Прогрев JVM
                    for (int i = 0; i < warmupRuns; i++) {
                        analyzer.analyze(directory, threads);
                    }

                    // 2. Измерения
                    List<Double> times = new ArrayList<>();
                    boolean isCorrect = true;

                    for (int i = 0; i < measurementRuns; i++) {
                        long start = System.nanoTime();
                        Map<String, Long> result = analyzer.analyze(directory, threads);
                        long end = System.nanoTime();

                        double timeSeconds = (end - start) / 1_000_000_000.0;
                        times.add(timeSeconds);

                        // Проверяем корректность на последнем прогоне
                        if (i == measurementRuns - 1) {
                            isCorrect = StatisticsVerifier.verifySilent(result, expectedFile);
                        }
                    }

                    double avgTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    String correctStr = isCorrect ? "true" : "false";

                    System.out.printf("  -> Среднее время: %.4f сек. | Корректно: %b%n", avgTime, isCorrect);

                    writer.write(String.format(Locale.US, "%s,%d,%.4f,%s%n", analyzer.getVariantName(), threads, avgTime, correctStr));
                }
            }
        }
        System.out.println("\n Результаты успешно сохранены в: " + csvFile.toAbsolutePath());
    }
}