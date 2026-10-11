package ru.seva.lab2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class StatisticsVerifier {

    public static boolean verify(Map<String, Long> actual, Path expectedFile) throws IOException {
        Map<String, Long> expected = new HashMap<>();

        try (var lines = Files.lines(expectedFile)) {
            lines.skip(1) // Пропускаем заголовок "word count"
                    .forEach(line -> {
                        String[] parts = line.split(" ");
                        if (parts.length == 2) {
                            expected.put(parts[0], Long.parseLong(parts[1]));
                        }
                    });
        }

        if (actual.size() != expected.size()) {
            System.out.printf("Ошибка: разное количество слов. Ожидалось: %d, получено: %d%n",
                    expected.size(), actual.size());
            return false;
        }

        for (Map.Entry<String, Long> entry : expected.entrySet()) {
            Long actualCount = actual.get(entry.getKey());
            if (actualCount == null || !actualCount.equals(entry.getValue())) {
                System.out.printf("Ошибка для слова '%s'. Ожидалось: %d, получено: %s%n",
                        entry.getKey(), entry.getValue(), actualCount);
                return false;
            }
        }

        System.out.println("Результат полностью совпадает с эталоном!");
        return true;
    }
}