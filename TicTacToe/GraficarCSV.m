clear, clc, close all

% Cargar el archivo CSV
data = readtable('trainingOvsRandom.csv');

% Extraer los datos de episodios, winRate y explorationRate
episodes = data.Episode;  % Columna de episodios
winRate = data.WinRate;   % Columna de tasas de victoria
explorationRate = data.ExplorationRate; % Columna de tasa de exploración

% Graficar la evolución del win rate y explorationRate
figure;
yyaxis left;  % Usar el eje izquierdo para winRate
plot(episodes, winRate, 'LineWidth', 2, 'DisplayName', 'Tasa de victorias');
ylabel('Tasa de victorias');

yyaxis right; % Usar el eje derecho para explorationRate
plot(episodes, explorationRate, 'LineWidth', 2, 'DisplayName', 'Tasa de exploración', 'LineStyle', '--');
ylabel('Tasa de exploración');

% Configurar el gráfico
grid on;
xlabel('Episodios');
title('Evolución del win rate y la tasa de exploración durante el entrenamiento');

% Añadir leyenda
legend show;

%%

% Cargar el archivo CSV
data = readtable('trainingOvsO.csv');

% Extraer los datos de episodios, winRate y explorationRate
episodes = data.Episode;  % Columna de episodios
winRate = data.WinRate;   % Columna de tasas de victoria
explorationRate = data.ExplorationRate; % Columna de tasa de exploración

% Graficar la evolución del win rate y explorationRate
figure;
yyaxis left;  % Usar el eje izquierdo para winRate
plot(episodes, winRate, 'LineWidth', 2, 'DisplayName', 'Tasa de victorias');
ylabel('Tasa de victorias');

yyaxis right; % Usar el eje derecho para explorationRate
plot(episodes, explorationRate, 'LineWidth', 2, 'DisplayName', 'Tasa de exploración', 'LineStyle', '--');
ylabel('Tasa de exploración');

% Configurar el gráfico
grid on;
xlabel('Episodios');
title('Evolución del win rate y la tasa de exploración durante el entrenamiento');

% Añadir leyenda
legend show;