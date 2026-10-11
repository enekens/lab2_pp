package ru.seva.lab2;

import ru.seva.lab2.analyzers.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        Path directory = Paths.get("generated-data");
        Path expectedFile = directory.resolve("expected-statistics.txt");

        int threadCount = 8;

        // Список всех реализованных на данный момент анализаторов
        WordAnalyzer[] analyzers = {
                new SequentialAnalyzer(),
                // Игнорируем нерабочий HashMap
                new SynchronizedMapAnalyzer(),
                new ConcurrentHashMapGetPutAnalyzer(),
                new ConcurrentHashMapMergeAnalyzer(),
                new LongAdderAnalyzer(),
                new LocalMapsAnalyzer(),
        };

        System.out.println("Начало тестирования...\n");

        for (WordAnalyzer analyzer : analyzers) {
            System.out.println("=== Вариант: " + analyzer.getVariantName() + " ===");

            long start = System.nanoTime();
            Map<String, Long> result = analyzer.analyze(directory, threadCount);
            long end = System.nanoTime();

            double timeSeconds = (end - start) / 1_000_000_000.0;
            System.out.printf("Время выполнения: %.4f сек.%n", timeSeconds);

            // Проверка корректности
            boolean isCorrect = StatisticsVerifier.verify(result, expectedFile);
            System.out.println("Результат проверки: " + (isCorrect ? "КОРРЕКТНО" : "НЕКОРРЕКТНО"));
            System.out.println();
        }
    }
}