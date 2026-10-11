import pandas as pd
import matplotlib.pyplot as plt
import os

# Загрузка данных
csv_path = 'results/measurements.csv'
df = pd.read_csv(csv_path)

# Оставляем только корректные реализации для графиков ускорения и эффективности
correct_df = df[df['correct'] == True].copy()

# Находим базовое время последовательной версии (T1)
t1 = correct_df[correct_df['variant'] == 'sequential']['time_seconds'].values[0]

# Расчет ускорения и эффективности
correct_df['speedup'] = t1 / correct_df['time_seconds']
correct_df['efficiency'] = (correct_df['speedup'] / correct_df['threads']) * 100

# Исключаем sequential из графиков многопоточности, оставляем только параллельные
parallel_df = correct_df[correct_df['variant'] != 'sequential']

os.makedirs('results', exist_ok=True)

# 1. График времени выполнения
plt.figure(figsize=(10, 6))
for variant in parallel_df['variant'].unique():
    subset = parallel_df[parallel_df['variant'] == variant]
    plt.plot(subset['threads'], subset['time_seconds'], marker='o', label=variant)

plt.title('Время выполнения в зависимости от количества потоков')
plt.xlabel('Количество потоков')
plt.ylabel('Время (секунды)')
plt.xticks([1, 2, 4, 8, 16, 32])
plt.legend()
plt.grid(True)
plt.savefig('results/time.png', dpi=300)
plt.close()

# 2. График ускорения
plt.figure(figsize=(10, 6))
for variant in parallel_df['variant'].unique():
    subset = parallel_df[parallel_df['variant'] == variant]
    plt.plot(subset['threads'], subset['speedup'], marker='s', label=variant)

# Идеальное линейное ускорение для справки
ideal_threads = [1, 2, 4, 8, 16, 32]
plt.plot(ideal_threads, ideal_threads, 'k--', label='Идеальное ускорение (S=p)')

plt.title('Ускорение в зависимости от количества потоков')
plt.xlabel('Количество потоков')
plt.ylabel('Ускорение (S_p)')
plt.xticks([1, 2, 4, 8, 16, 32])
plt.legend()
plt.grid(True)
plt.savefig('results/speedup.png', dpi=300)
plt.close()

# 3. График эффективности
plt.figure(figsize=(10, 6))
for variant in parallel_df['variant'].unique():
    subset = parallel_df[parallel_df['variant'] == variant]
    plt.plot(subset['threads'], subset['efficiency'], marker='^', label=variant)

plt.title('Эффективность в зависимости от количества потоков')
plt.xlabel('Количество потоков')
plt.ylabel('Эффективность (%)')
plt.xticks([1, 2, 4, 8, 16, 32])
plt.legend()
plt.grid(True)
plt.savefig('results/efficiency.png', dpi=300)
plt.close()

print("Графики успешно сохранены в папку results/")