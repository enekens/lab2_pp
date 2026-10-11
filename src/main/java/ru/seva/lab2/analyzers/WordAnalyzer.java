package ru.seva.lab2.analyzers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface WordAnalyzer {
    /**
     * Анализирует текстовые файлы в указанной директории.
     *
     * @param directory путь к папке с текстовыми файлами
     * @param threadCount количество потоков для обработки
     * @return частотный словарь (слово -> количество вхождений)
     */
    Map<String, Long> analyze(Path directory, int threadCount)
            throws IOException, InterruptedException, ExecutionException;

    /**
     * Возвращает название варианта реализации для логирования и отчетов.
     */
    String getVariantName();
}