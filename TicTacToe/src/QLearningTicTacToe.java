import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.io.*;

public class QLearningTicTacToe {

	private char[][] board;
	private HashMap<String, HashMap<String, Double>> qTableX;
	private HashMap<String, HashMap<String, Double>> qTableO;
	private double explorProb; // Probabilidad de exploración
	private double learningRate;
	private double discountFactor;

	public QLearningTicTacToe() {

		board = new char[3][3];

		qTableX = new HashMap<>();
		qTableO = new HashMap<>();

		explorProb = 0.9;
		learningRate = 0.1;
		discountFactor = 0.5;

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {

				board[i][j] = ' ';
			}
		}
	}

	// Convierte el estado actual a un String
	private String getState() {

		StringBuilder state = new StringBuilder();

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {

				state.append(board[i][j]);
			}
		}

		return state.toString();
	}

	// Registra el estado actual en la tabla Q
	private void registerState(HashMap<String, HashMap<String, Double>> qTable) {

		String state = getState();

		if (!qTable.containsKey(state)) {

			HashMap<String, Double> actions = new HashMap<>();

			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {

					if (board[i][j] == ' ') {
						// Da un sesgo inicial al movimiento del centro en el estado inicial
						if (state.equals("         ") && i == 1 && j == 1) {
							actions.put(i + "," + j, 0.1); // Sesgo positivo para el centro
						} else {
							actions.put(i + "," + j, 0.0); // Otros movimientos empiezan en 0
						}
					}
				}
			}

			qTable.put(state, actions);
		}
	}

	// Elige una acción a partir de un estado
	private String chooseAction(String state, HashMap<String, HashMap<String, Double>> qTable) {

		Random random = new Random(System.currentTimeMillis());

		registerState(qTable); // Aseguramos que el estado está en la tabla Q

		Map<String, Double> actions = qTable.get(state);

		if (random.nextDouble() < explorProb) { // Exploración: elige una acción aleatoria

			Object[] keys = actions.keySet().toArray();

			return (String) keys[random.nextInt(keys.length)];

		} else { // Explotación: elige la acción con el mayor valor en la tabla Q

			/*
			 * double maxQValue = Double.NEGATIVE_INFINITY; String bestAction = null;
			 * 
			 * for (Map.Entry<String, Double> entry : actions.entrySet()) {
			 * 
			 * if (entry.getValue() > maxQValue) { maxQValue = entry.getValue(); bestAction
			 * = entry.getKey(); } }
			 * 
			 * return bestAction;
			 */

			// Encuentra las acciones con el valor Q máximo
			double maxQ = Collections.max(actions.values());
			List<String> bestActions = new ArrayList<>();

			for (Map.Entry<String, Double> entry : actions.entrySet()) {
				if (entry.getValue() == maxQ) {
					bestActions.add(entry.getKey());
				}
			}

			// Si hay varias acciones con el mismo valor Q máximo, elige una aleatoriamente
			if (bestActions.size() > 1) {
				return bestActions.get(random.nextInt(bestActions.size()));
			} else {
				return bestActions.get(0);
			}
		}
	}

	// Realiza un movimiento
	private boolean makeMove(String action, char player) {

		String[] parts = action.split(",");

		int row = Integer.parseInt(parts[0]);
		int col = Integer.parseInt(parts[1]);

		if (board[row][col] == ' ') {
			board[row][col] = player;
			return true;
		}

		return false;
	}

	// Obtiene las acciones posibles dado el estado actual del tablero
	private List<String> getPossibleActions() {

		List<String> actions = new ArrayList<>();

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {

				if (board[i][j] == ' ')
					actions.add(i + "," + j);
			}
		}

		return actions;
	}

	// Comprueba el estado del juego
	private String checkGameState() {

		// Comprueba filas y columnas
		for (int i = 0; i < 3; i++) {

			if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != ' ')
				return String.valueOf(board[i][0]);

			if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != ' ')
				return String.valueOf(board[0][i]);
		}

		// Comprueba las diagonales
		if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != ' ')
			return String.valueOf(board[0][0]);

		if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != ' ')
			return String.valueOf(board[0][2]);

		// Comprueba si hay empate
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {

				if (board[i][j] == ' ')
					return "ongoing";
			}
		}

		return "draw";
	}

	// Actualiza la tabla Q
	private void updateQValue(String state, String action, String nextState, double reward,
			HashMap<String, HashMap<String, Double>> qTable) {

		registerState(qTable);

		double currentQ = qTable.get(state).get(action);
		double maxNextQ = qTable.get(nextState).values().stream().max(Double::compare).orElse(0.0);

		double updatedQ = currentQ + learningRate * (reward + discountFactor * maxNextQ - currentQ);

		qTable.get(state).put(action, updatedQ);
	}

	// Entrena el modelo X
	public void trainX(int episodes, boolean randomOponent) {

		int winCount = 0;
		int drawCount = 0;

		// Lista para almacenar el win rate y la tasa de exploración cada 1000 episodios
		List<Double> winRates = new ArrayList<>();
		List<Double> explorRates = new ArrayList<>();

		for (int episode = 0; episode < episodes; episode++) {

			resetBoard();

			String state, nextState;
			String result = "ongoing";

			while (result.equals("ongoing")) {

				// Turno del agente
				state = getState();
				String action = chooseAction(state, qTableX);
				makeMove(action, 'X');
				result = checkGameState();

				if (result.equals("ongoing")) {

					nextState = getState();
					updateQValue(state, action, nextState, 0.0, qTableX);

				} else {

					nextState = getState();
					double reward = result.equals("X") ? 1.0 : result.equals("draw") ? 0.5 : -1.0;
					updateQValue(state, action, nextState, reward, qTableX);

					break;
				}

				// Turno del oponente
				String opponentAction = null;

				if (randomOponent) {

					Random random = new Random(System.currentTimeMillis());

					List<String> availableActions = getPossibleActions();
					opponentAction = availableActions.get(random.nextInt(availableActions.size()));
				} else
					opponentAction = chooseAction(nextState, qTableX);

				makeMove(opponentAction, 'O');
				result = checkGameState();

				if (!result.equals("ongoing")) {

					double reward = result.equals("X") ? 1.0 : result.equals("draw") ? 0.5 : -1.0;

					// if (randomOponent)
					updateQValue(state, action, nextState, reward, qTableX);
				}
			}

			// Actualizamos los contadores según el resultado
			if (result.equals("X"))
				winCount++;
			else if (result.equals("draw"))
				drawCount++;

			// Cada 1000 episodios, calculamos el win rate y lo almacenamos
			if ((episode + 1) % 1000 == 0) {

				double winRate = (double) winCount / (episode + 1) * 100;
				winRates.add(winRate);
				explorRates.add(explorProb);
			}

			// Reducimos la probabilidad de exploración durante el entrenamiento
			if (explorProb > 0.1) {
				explorProb -= 0.000001;
			}
		}

		double winPercentage = (double) winCount / episodes * 100;
		double drawPercentage = (double) drawCount / episodes * 100;

		System.out.println("\nEntrenamiento de X completado:");
		System.out.println("Porcentaje de victorias: " + String.format("%.2f", winPercentage) + "%");
		System.out.println("Porcentaje de empates: " + String.format("%.2f", drawPercentage) + "%\n");

		// Exportar los datos del win rate a un archivo CSV
		if (randomOponent)
			exportCSV(winRates, explorRates, "trainingXvsRandom.csv");
		else
			exportCSV(winRates, explorRates, "trainingXvsX.csv");
	}

	// Entrena el modelo O
	public void trainO(int episodes, boolean randomOponent) {

		int winCount = 0;
		int drawCount = 0;

		// Lista para almacenar el win rate y la tasa de exploración cada 1000 episodios
		List<Double> winRates = new ArrayList<>();
		List<Double> explorRates = new ArrayList<>();

		for (int episode = 0; episode < episodes; episode++) {

			resetBoard();

			String state = null, nextState, action = null;
			String result = "ongoing";

			while (result.equals("ongoing")) {

				// Turno del oponente
				String opponentAction = null;

				if (randomOponent) {

					Random random = new Random(System.currentTimeMillis());

					List<String> availableActions = getPossibleActions();
					opponentAction = availableActions.get(random.nextInt(availableActions.size()));
				} else
					opponentAction = chooseAction(getState(), qTableO);

				makeMove(opponentAction, 'X');
				result = checkGameState();

				if (!result.equals("ongoing")) {

					double reward = result.equals("O") ? 1.0 : result.equals("draw") ? 0.5 : -1.0;

					nextState = getState();

					// if (randomOponent)
					updateQValue(state, action, nextState, reward, qTableO);

					break;
				}

				// Turno del agente
				state = getState();
				action = chooseAction(state, qTableO);
				makeMove(action, 'O');
				result = checkGameState();

				if (result.equals("ongoing")) {

					nextState = getState();
					updateQValue(state, action, nextState, 0.0, qTableO);

				} else {

					nextState = getState();
					double reward = result.equals("O") ? 1.0 : result.equals("draw") ? 0.5 : -1.0;
					updateQValue(state, action, nextState, reward, qTableO);
				}
			}

			// Actualizamos los contadores según el resultado
			if (result.equals("O"))
				winCount++;
			else if (result.equals("draw"))
				drawCount++;

			// Cada 1000 episodios, calculamos el win rate y lo almacenamos
			if ((episode + 1) % 1000 == 0) {

				double winRate = (double) winCount / (episode + 1) * 100;
				winRates.add(winRate);
				explorRates.add(explorProb);
			}

			// Reducimos la probabilidad de exploración durante el entrenamiento
			if (explorProb > 0.1) {
				explorProb -= 0.000001;
			}
		}

		double winPercentage = (double) winCount / episodes * 100;
		double drawPercentage = (double) drawCount / episodes * 100;

		System.out.println("\nEntrenamiento de O completado:");
		System.out.println("Porcentaje de victorias: " + String.format("%.2f", winPercentage) + "%");
		System.out.println("Porcentaje de empates: " + String.format("%.2f", drawPercentage) + "%\n");

		// Exportar los datos del win rate a un archivo CSV
		if (randomOponent)
			exportCSV(winRates, explorRates, "trainingOvsRandom.csv");
		else
			exportCSV(winRates, explorRates, "trainingOvsO.csv");
	}
	
	
	// Entrena X vs O
	public void trainXvsO(int episodes) {

		int winCountX = 0;
		int winCountO = 0;
		int drawCount = 0;

		// Lista para almacenar el win rate y la tasa de exploración cada 1000 episodios
		List<Double> winRatesX = new ArrayList<>();
		List<Double> winRatesO = new ArrayList<>();
		List<Double> explorRates = new ArrayList<>();

		for (int episode = 0; episode < episodes; episode++) {

			resetBoard();

			String stateX, nextStateX, stateO = null, nextStateO = null, actionX, actionO = null;
			String result = "ongoing";
			double reward;

			while (result.equals("ongoing")) {

				// Turno del agente X
				stateX = getState();
				actionX = chooseAction(stateX, qTableX);
				makeMove(actionX, 'X');
				result = checkGameState();

				if (result.equals("ongoing")) {

					nextStateX = getState();
					updateQValue(stateX, actionX, nextStateX, 0.0, qTableX);

				} else {

					nextStateX = getState();
					reward = result.equals("X") ? 1.0 : result.equals("draw") ? 0.5 : -1.0;
					updateQValue(stateX, actionX, nextStateX, reward, qTableX);
					
					reward = result.equals("X") ? -1.0 : result.equals("draw") ? 0.5 : 1.0;
					updateQValue(stateO, actionO, nextStateO, reward, qTableO);

					break;
				}

				// Turno del agente O
				stateO = getState();
				actionO = chooseAction(stateO, qTableO);
				makeMove(actionO, 'O');
				result = checkGameState();

				if (result.equals("ongoing")) {

					nextStateO = getState();
					updateQValue(stateO, actionO, nextStateO, 0.0, qTableO);

				} else {

					nextStateO = getState();
					reward = result.equals("X") ? -1.0 : result.equals("draw") ? 0.5 : 1.0;
					updateQValue(stateO, actionO, nextStateO, reward, qTableO);
					
					reward = result.equals("X") ? 1.0 : result.equals("draw") ? 0.5 : -1.0;
					updateQValue(stateX, actionX, nextStateX, reward, qTableX);

					break;
				}
			}

			// Actualizamos los contadores según el resultado
			if (result.equals("X"))
				winCountX++;
			else if (result.equals("O"))
				winCountO++;
			else if (result.equals("draw"))
				drawCount++;

			// Cada 1000 episodios, calculamos el win rate y lo almacenamos
			if ((episode + 1) % 1000 == 0) {

				double winRateX = (double) winCountX / (episode + 1) * 100;
				winRatesX.add(winRateX);
				
				double winRateO = (double) winCountO / (episode + 1) * 100;
				winRatesO.add(winRateO);

				explorRates.add(explorProb);
			}

			// Reducimos la probabilidad de exploración durante el entrenamiento
			if (explorProb > 0.1) {
				explorProb -= 0.000001;
			}
		}

		double winPercentageX = (double) winCountX / episodes * 100;
		double winPercentageO = (double) winCountO / episodes * 100;
		double drawPercentage = (double) drawCount / episodes * 100;

		System.out.println("\nEntrenamiento de X vs O completado:");
		System.out.println("Porcentaje de victorias de X: " + String.format("%.2f", winPercentageX) + "%");
		System.out.println("Porcentaje de victorias de O: " + String.format("%.2f", winPercentageO) + "%");
		System.out.println("Porcentaje de empates: " + String.format("%.2f", drawPercentage) + "%\n");

		exportCSV(winRatesX, explorRates, "trainingXvsO_winratesX.csv");
		exportCSV(winRatesO, explorRates, "trainingXvsO_winratesO.csv");
	}

	// Exporta el win rate a un archivo CSV
	public void exportCSV(List<Double> winRates, List<Double> explorRates, String filename) {

		try (PrintWriter writer = new PrintWriter(new File(filename))) {

			StringBuilder sb = new StringBuilder();

			sb.append("Episode,WinRate,ExplorationRate\n");

			for (int i = 0; i < winRates.size(); i++) {
				sb.append((i + 1) * 1000).append(",").append(winRates.get(i)).append(",").append(explorRates.get(i))
						.append("\n");
			}

			writer.write(sb.toString());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Muestra el tablero
	private void displayBoard() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {

				System.out.print(board[i][j]);

				if (j < 2)
					System.out.print(" | ");
			}

			System.out.println();

			if (i < 2)
				System.out.println("--+---+--");
		}
	}

	// Reinicia el tablero
	private void resetBoard() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				board[i][j] = ' ';
			}
		}
	}

	// Juega contra el agente
	public void playAgainstBot(char model) {

		Scanner scanner = new Scanner(System.in);
		String result = "ongoing";

		resetBoard();
		explorProb = 0.0; // Para que el agente elija siempre su mejor movimiento

		System.out.println();

		while (result.equals("ongoing")) {

			if (model == 'X') {

				// Turno del agente (X)
				String state = getState();
				String action = chooseAction(state, qTableX);

				makeMove(action, 'X');
				result = checkGameState();
				displayBoard();

				if (result.equals("ongoing")) {

					// Turno del jugador humano (O)
					System.out.println("Introduce tu movimiento (fila,columna):");
					String humanMove = scanner.nextLine();

					while (!makeMove(humanMove, 'O')) {

						System.out.println("ERROR: La posición ya está ocupada.");
						System.out.println("Introduce tu movimiento (fila,columna):");
						humanMove = scanner.nextLine();
					}
					result = checkGameState();
				}
			} else {

				displayBoard();

				// Turno del jugador humano (X)
				System.out.println("Introduce tu movimiento (fila,columna):");
				String humanMove = scanner.nextLine();

				while (!makeMove(humanMove, 'X')) {

					System.out.println("ERROR: La posición ya está ocupada.");
					System.out.println("Introduce tu movimiento (fila,columna):");
					humanMove = scanner.nextLine();
				}
				result = checkGameState();

				if (result.equals("ongoing")) {

					// Turno del agente (O)
					String state = getState();
					String action = chooseAction(state, qTableO);

					makeMove(action, 'O');
					result = checkGameState();
				}
			}
		}

		// Mostramos el resultado final
		displayBoard();

		if ((result.equals("X") && model == 'X') || (result.equals("O") && model == 'O')) {
			System.out.println("¡El agente ganó!");
		} else if ((result.equals("X") && model == 'O') || (result.equals("O") && model == 'X')) {
			System.out.println("¡Tú ganaste!");
		} else {
			System.out.println("¡Empate!");
		}

	}

	// Evalúa el rendimiento del modelo contra un bot aleatorio
	public void evaluateAgainstRandomBot(int games, char model) {

		int winCount = 0;
		int drawCount = 0;

		explorProb = 0.0; // Para que el agente elija siempre su mejor acción

		Random random = new Random(33);

		System.out.println();

		for (int game = 0; game < games; game++) {

			resetBoard();

			String result = "ongoing";

			while (result.equals("ongoing")) {

				// Random random = new Random(System.currentTimeMillis());

				if (model == 'X') {

					// Turno del agente (X)
					String state = getState();
					String action = chooseAction(state, qTableX);
					makeMove(action, 'X');
					result = checkGameState();

					if (!result.equals("ongoing"))
						break;

					// Turno del bot aleatorio (O)
					List<String> availableActions = getPossibleActions();
					String randomMove = availableActions.get(random.nextInt(availableActions.size()));
					makeMove(randomMove, 'O');
					result = checkGameState();
				} else {

					// Turno del bot aleatorio (X)
					List<String> availableActions = getPossibleActions();
					String randomMove = availableActions.get(random.nextInt(availableActions.size()));
					makeMove(randomMove, 'X');
					result = checkGameState();

					if (!result.equals("ongoing"))
						break;

					// Turno del agente (O)
					String state = getState();
					String action = chooseAction(state, qTableO);
					makeMove(action, 'O');
					result = checkGameState();
				}
			}

			if ((result.equals("X") && model == 'X') || (result.equals("O") && model == 'O')) {
				winCount++;
			} else if (result.equals("draw")) {
				drawCount++;
			}
		}

		double winPercentage = (double) winCount / games * 100;
		double drawPercentage = (double) drawCount / games * 100;

		System.out.println("Evaluación de " + model + " contra bot aleatorio:");
		System.out.println("Porcentaje de victorias: " + String.format("%.2f", winPercentage) + "%");
		System.out.println("Porcentaje de empates: " + String.format("%.2f", drawPercentage) + "%");
	}

	// Guarda el modelo final
	public void saveModel(String filePath, HashMap<String, HashMap<String, Double>> qTable) throws IOException {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

			for (Map.Entry<String, HashMap<String, Double>> entry : qTable.entrySet()) {

				String state = entry.getKey();
				Map<String, Double> actions = entry.getValue();

				// Encuentra la mejor acción para el estado actual
				String bestAction = actions.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
						.orElse("NoAction");

				writer.write(state + ":" + bestAction);
				writer.newLine();
			}
		}

		System.out.println("Modelo guardado correctamente en: " + filePath);
	}

	// Guarda la Tabla Q
	public void saveQTable(String filePath, HashMap<String, HashMap<String, Double>> qTable) throws IOException {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

			for (Map.Entry<String, HashMap<String, Double>> entry : qTable.entrySet()) {

				String state = entry.getKey();
				Map<String, Double> actions = entry.getValue();

				writer.write(state + ":");

				for (Map.Entry<String, Double> actionEntry : actions.entrySet()) {
					writer.write(actionEntry.getKey() + "=" + actionEntry.getValue() + "/");
				}

				writer.newLine();
			}
		}

		System.out.println("Tabla Q guardada correctamente en: " + filePath);
	}

	// Carga un modelo
	public void loadModel(String filePath, char model) throws IOException {

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

			if (model == 'X')
				qTableX.clear();
			else
				qTableO.clear();

			String line;

			while ((line = reader.readLine()) != null) {

				String[] parts = line.split(":");
				String state = parts[0];
				String bestAction = parts[1];

				HashMap<String, Double> actions = new HashMap<>();
				actions.put(bestAction, 1.0); // Asigna un valor arbitrario alto para la mejor acción

				if (model == 'X')
					qTableX.put(state, actions);
				else
					qTableO.put(state, actions);
			}
		}

		System.out.println("Modelo " + model + " cargado correctamente desde: " + filePath);
	}

	// Carga una QTable desde un archivo
	public void loadQTable(String filePath, char model) throws IOException {

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

			if (model == 'X')
				qTableX.clear();
			else
				qTableO.clear();

			String line;

			while ((line = reader.readLine()) != null) {

				String[] parts = line.split(":");

				if (parts.length == 2) {

					String state = parts[0].trim(); // Estado
					String actionsPart = parts[1].trim(); // Acciones y valores

					HashMap<String, Double> actions = new HashMap<>();
					String[] actionPairs = actionsPart.split("/"); // Dividir las acciones y valores

					for (String actionPair : actionPairs) {

						if (!actionPair.isEmpty()) {

							String[] actionValue = actionPair.split("=");

							String action = actionValue[0].trim(); // Acción
							double value = Double.parseDouble(actionValue[1].trim()); // Valor Q

							actions.put(action, value); // Guardar acción y valor Q
						}
					}

					if (model == 'X')
						qTableX.put(state, actions);
					else
						qTableO.put(state, actions);
				}
			}
		}

		System.out.println("QTable " + model + " cargada correctamente desde: " + filePath);
	}

	public static void main(String[] args) throws IOException {

		Scanner scanner = new Scanner(System.in);

		QLearningTicTacToe game = new QLearningTicTacToe();

		// ENTRENAMIENTO DE X
		
		game.trainX(2000000, true); // Entrenamiento de X con 2.000.000 episodios contra el oponente aleatorio

		game.explorProb = 0.9;

		game.trainX(1000000, false); // Entrenamiento de X con 1.000.000 episodios contra sí mismo
		 
		game.saveModel("ModelX.txt", game.qTableX);
		game.saveQTable("QTableX.txt", game.qTableX);
		
		//game.loadModel("BestModelX.txt", 'X');

		game.evaluateAgainstRandomBot(1000000, 'X'); // Evaluación de X con 1.000.000 de partidas

		// ENTRENAMIENTO DE O
		
		game.trainO(2000000, true); // Entrenamiento de O con 1.000.000 episodios contra el oponente aleatorio

		game.explorProb = 0.9;

		game.trainO(1000000, false); // Entrenamiento de O con 1.000.000 episodios contra sí mismo

		game.saveModel("ModelO.txt", game.qTableO);
		game.saveQTable("QTableO.txt", game.qTableO);

		game.evaluateAgainstRandomBot(1000000, 'O'); // Evaluación de O con 1.000.000 de partidas
		
		// ENTRENAMIENTO DE X vs O
		
		game.trainXvsO(1000000);
		
		game.evaluateAgainstRandomBot(1000000, 'X');
		game.evaluateAgainstRandomBot(1000000, 'O');
		

		char[] a;
		char bot = 'X';

		do {

			game.playAgainstBot(bot);

			System.out.println("\n¿Quieres jugar otra partida? (S/N)");
			String answer = scanner.nextLine();

			a = answer.toUpperCase().toCharArray();

		} while (a[0] == 'S');
	}
}
