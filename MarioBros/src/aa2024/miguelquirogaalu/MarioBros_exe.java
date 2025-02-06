package aa2024.miguelquirogaalu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tools.Utils;
import tracks.ArcadeMachine;

public class MarioBros_exe {
	
	public static void main(String[] args) throws IOException {

		String p0 = "aa2024.miguelquirogaalu.Agente";

		// Load available games
		String spGamesCollection = "examples/all_games_sp.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		// Game settings
		boolean visuals = true;
		int seed = new Random().nextInt();

		// Game and level to play
		int gameIdx = 118;
		int levelIdx = 3; // level names from 0 to 4 (game_lvlN.txt).

		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];
		String level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
		
		Mundo118.visualizar = visuals;
		Agente.visualizar = visuals;
		
		QLearning.loadQTable("QTable_300_TOP.txt");
		
		ArcadeMachine.runOneGame(game, level1, visuals, p0, null, seed, 0);
	 	//ArcadeMachine.playOneGame(game, level1, null, seed);
			
		QLearning.muestraContadorEstados();
		QLearning.exportarContadorEstadosCSV("ContadorEstadosLvl" + levelIdx + ".csv");

		System.exit(0);
	}
	
	/*
	public static void main(String[] args) throws IOException {

		String p0 = "aa2024.miguelquirogaalu.Agente";

		// Load available games
		String spGamesCollection = "examples/all_games_sp.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		// Game settings
		boolean visuals = false;
		int seed = new Random().nextInt();

		// Game and level to play
		int gameIdx = 118;
		
		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];

		double[] array;
		int wins = 0;
		int score = 0;
		int epocas = 300;
		
		Mundo118.visualizar = visuals;
		Agente.visualizar = visuals;
		
		List<Integer> scores = new ArrayList<Integer>();
		List<Double> explorRates = new ArrayList<Double>();

		for (int i = 0; i < epocas; i++) {
			
			if (i != 0)
				QLearning.loadQTable("QTable.txt");
			
			int levelIdx = new Random(System.currentTimeMillis()).nextInt(5);
			String level = game.replace(gameName, gameName + "_lvl" + levelIdx);
			
			System.out.println("ÉPOCA -> " + (i+1) + " - Nivel -> " + levelIdx);
			
			array = ArcadeMachine.runOneGame(game, level, visuals, p0, null, seed, 0);
		 	//array = ArcadeMachine.playOneGame(game, level1, null, seed);

			if (array[0] == 1) { // si he ganado
				wins++;
				
				long estadoUltimo = Mundo118.historialEstados.pop();
				long estadoPenultimo = Mundo118.historialEstados.pop();
				
				QLearning.actualizaTablaQ(estadoPenultimo, Mundo118.historialAcciones.peek(), estadoUltimo, 1);
			}
			
			if (array[0] == 0) { // si me han matado
				
				long estadoUltimo = Mundo118.historialEstados.pop();
				long estadoPenultimo = Mundo118.historialEstados.pop();
				
				QLearning.actualizaTablaQ(estadoPenultimo, Mundo118.historialAcciones.peek(), estadoUltimo, -1);
			}

			score += (int) array[1];
			
			QLearning.saveQTable("QTable.txt");
			
			scores.add((int) array[1]);
			explorRates.add(QLearning.probExplor);
			
			if (QLearning.probExplor > 0.1) QLearning.probExplor = QLearning.probExplor - ((double) 0.9 / epocas);
		}
		
		QLearning.exportarCSV(scores, explorRates, ("GraficaTraining.csv"));
		
		QLearning.muestraContadorEstados();

		System.out.println("\nPuntos (media): " + score / epocas);
		System.out.println("Puntos totales: " + score);
		System.out.println("\nWIN RATE: " + wins * 100 / epocas + "%");

		System.exit(0);
	}
	*/
}
