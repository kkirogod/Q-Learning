clear, clc

% Cargar el archivo CSV
data = readtable('ContadorEstadosLvl4.csv');

estados = data.Estado;
contadores = data.Contador;

bar(categorical(estados), contadores);

xlabel('Estados');
ylabel('Conteo');
title('Histograma de Estados');
grid on;

%%

clear, clc

% Cargar el archivo CSV
data = readtable('GraficaTraining.csv');

% Extraer los datos de episodios, score y explorationRate
episodes = data.Episode;  % Columna de episodios
score = data.Score;   % Columna de score
explorationRate = data.ExplorationRate; % Columna de tasa de exploración

% Calcular la tendencia del score con una media móvil
windowSize = 10; % Tamaño de la ventana para la media móvil
scoreTrend = movmean(score, windowSize); % Suavizado con media móvil

% Calcular la regresión lineal del score
coeffs = polyfit(episodes, score, 1); % Ajustar una línea (grado 1)
linearFit = polyval(coeffs, episodes); % Evaluar la línea ajustada

% Graficar la evolución del score y explorationRate
figure;
yyaxis left;  % Usar el eje izquierdo para score
plot(episodes, score, 'LineWidth', 2, 'DisplayName', 'Score');
hold on;
plot(episodes, scoreTrend, 'LineWidth', 2, 'DisplayName', 'Tendencia del Score (Media Móvil)', 'Color', 'r');
plot(episodes, linearFit, 'LineWidth', 2, 'DisplayName', 'Regresión Lineal del Score', 'Color', 'g'); % Línea continua
ylabel('Score');

yyaxis right; % Usar el eje derecho para explorationRate
plot(episodes, explorationRate, 'LineWidth', 2, 'DisplayName', 'Tasa de exploración', 'LineStyle', '--');
ylabel('Tasa de exploración');

% Configurar el gráfico
grid on;
xlabel('Episodios');
title('Evolución del score y la tasa de exploración durante el entrenamiento');

% Añadir leyenda
legend show;