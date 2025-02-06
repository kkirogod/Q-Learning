package aa2024.miguelquirogaalu;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ontology.Types.ACTIONS;

import java.io.*;

public class QLearning {

	private static Mundo118 mundo;
	public static HashMap<Long, HashMap<ACTIONS, Double>> qTable = new HashMap<>();
	public static double probExplor= 1.0; // Probabilidad de exploración
	private static double learningRate = 0.1;
	private static double discountFactor = 0.5;
	
	public static HashMap<Long, Integer> contadorEstados = new HashMap<Long, Integer>(); // para saber cuentas veces se repite cada estado
	
	public static void inicializar(Mundo118 m) {
		
		mundo = m;
		contadorEstados = new HashMap<Long, Integer>();
	}

	// Registra un nuevo estado en la tabla Q
	public static void registrarEstado(long nuevoEstado) {

		if (!qTable.containsKey(nuevoEstado)) {

			HashMap<ACTIONS, Double> acciones = new HashMap<>();
			
			Random rnd = new Random(System.currentTimeMillis());

			for(ACTIONS accion : mundo.accionesDisponibles())
				acciones.put(accion, rnd.nextDouble()*0);

			qTable.put(nuevoEstado, acciones);
		}
	}

	// Elige una acción a partir de un estado
	public static ACTIONS eligeAccion(long estado) {
		
		if (!contadorEstados.containsKey(estado)) {
			contadorEstados.put(estado, 1);
		}
		else {
			contadorEstados.put(estado, (contadorEstados.get(estado) + 1));
		}

		Random random = new Random(System.currentTimeMillis());

		//registrarEstado(estado); // Aseguramos que el estado esté en la tabla Q

		Map<ACTIONS, Double> acciones = qTable.get(estado);

		if (random.nextDouble() < probExplor) { // Exploración: elige una acción aleatoria

			Object[] keys = acciones.keySet().toArray();

			return (ACTIONS) keys[random.nextInt(keys.length)];

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
			double maxQ = Collections.max(acciones.values());
			
			List<ACTIONS> mejoresAcciones = new ArrayList<>();

			for (Map.Entry<ACTIONS, Double> accion : acciones.entrySet()) {
				
				if (accion.getValue() == maxQ)
					mejoresAcciones.add(accion.getKey());
			}

			// Si hay varias acciones con el mismo valor Q máximo, elige una aleatoriamente
			if (mejoresAcciones.size() > 1)
				return mejoresAcciones.get(random.nextInt(mejoresAcciones.size()));
			else
				return mejoresAcciones.get(0);
		}
	}
	
	public static ACTIONS eligeMejorAccion(long estado) {
		
		if (!contadorEstados.containsKey(estado)) {
			contadorEstados.put(estado, 1);
		}
		else {
			contadorEstados.put(estado, (contadorEstados.get(estado) + 1));
		}

		Map<ACTIONS, Double> acciones = qTable.get(estado);
		
		Random random = new Random(System.currentTimeMillis());

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
		double maxQ = Collections.max(acciones.values());
		
		List<ACTIONS> mejoresAcciones = new ArrayList<>();

		for (Map.Entry<ACTIONS, Double> accion : acciones.entrySet()) {
			
			if (accion.getValue() == maxQ)
				mejoresAcciones.add(accion.getKey());
		}

		// Si hay varias acciones con el mismo valor Q máximo, elige una aleatoriamente
		if (mejoresAcciones.size() > 1)
			return mejoresAcciones.get(random.nextInt(mejoresAcciones.size()));
		else
			return mejoresAcciones.get(0);
	}

	// Actualiza la tabla Q
	public static void actualizaTablaQ(long estado, ACTIONS accion, long estadoSig, int flag) {
		
		if (qTable.get(estado).containsKey(accion)) {

			double qValueActual = qTable.get(estado).get(accion);
			double maxQSig = qTable.get(estadoSig).values().stream().max(Double::compare).orElse(0.0);
			
			double nuevoQ = qValueActual + learningRate * (mundo.recompensa(estado, estadoSig, flag) + discountFactor * maxQSig - qValueActual);
	
			qTable.get(estado).put(accion, nuevoQ);
		}
	}

	// Exporta el score a un archivo .csv
	public static void exportarCSV(List<Integer> scores, List<Double> explorRates, String filename) {

		try (PrintWriter writer = new PrintWriter(new File(filename))) {

			StringBuilder sb = new StringBuilder();

			sb.append("Episode,Score,ExplorationRate\n");

			for (int i = 0; i < scores.size(); i++) {
				sb.append(i + 1).append(",").append(scores.get(i)).append(",").append(explorRates.get(i)).append("\n");
			}

			writer.write(sb.toString());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Guarda el modelo final
	public static void saveModelo(String filePath) throws IOException {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

			for (Map.Entry<Long, HashMap<ACTIONS, Double>> entry : qTable.entrySet()) {

				long estado = entry.getKey();
				Map<ACTIONS, Double> acciones = entry.getValue();

				// Encuentra la mejor acción para el estado actual
				ACTIONS mejorAccion = acciones.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
						.orElse(ACTIONS.ACTION_DOWN);

				writer.write(estado + ":" + (mejorAccion == ACTIONS.ACTION_DOWN ? "null" : mejorAccion.toString()));
				writer.newLine();
			}
		}

		System.out.println("Modelo guardado correctamente en: " + filePath);
	}

	// Guarda la Tabla Q
	public static void saveQTable(String filePath) throws IOException {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

			for (Map.Entry<Long, HashMap<ACTIONS, Double>> entry : qTable.entrySet()) {

				long estado = entry.getKey();
				Map<ACTIONS, Double> acciones = entry.getValue();

				writer.write(estado + ":");

				for (Map.Entry<ACTIONS, Double> accion : acciones.entrySet()) {
					writer.write(accion.getKey().toString() + "=" + accion.getValue().toString() + "/");
				}

				writer.newLine();
			}
		}

		System.out.println("Tabla Q guardada correctamente en: " + filePath);
	}
	
	// Muestra la Tabla Q
	public static void showQTable() {
		
		System.out.println("\n\n\t\t\t***** Q-Table *****\n");
		
		for (Map.Entry<Long, HashMap<ACTIONS, Double>> entry : qTable.entrySet()) {

			long estado = entry.getKey();
			Map<ACTIONS, Double> acciones = entry.getValue();

			System.out.print(estado + " -> ");

			for (Map.Entry<ACTIONS, Double> accion : acciones.entrySet()) {
				System.out.print(accion.getKey().toString() + "=" + accion.getValue().toString() + " || ");
			}

			System.out.println();
		}
	}

	// Carga un modelo
	public void loadModelo(String filePath) throws IOException {

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

			qTable.clear();

			String linea;

			while ((linea = reader.readLine()) != null) {

				String[] partes = linea.split(":");
				String estado = partes[0];
				String mejorAccion = partes[1];

				HashMap<ACTIONS, Double> acciones = new HashMap<>();
				acciones.put(ACTIONS.fromString(mejorAccion), 1.0); // Asigno un valor arbitrario alto para la mejor acción

				qTable.put(Long.valueOf(estado), acciones);
			}
		}

		System.out.println("Modelo cargado correctamente desde: " + filePath);
	}

	// Carga una QTable desde un archivo
	public static void loadQTable(String filePath) throws IOException {

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

			qTable.clear();

			String linea;

			while ((linea = reader.readLine()) != null) {

				String[] partes = linea.split(":");

				if (partes.length == 2) {

					long estado = Long.valueOf(partes[0].trim()); // Estado
					String accionesValores = partes[1].trim(); // Acciones y valores

					HashMap<ACTIONS, Double> acciones = new HashMap<>();
					String[] arrayAccionesValores = accionesValores.split("/"); // Dividir las acciones y valores

					for (String accionValor : arrayAccionesValores) {

						if (!accionValor.isEmpty()) {

							String[] arrayAccionValor = accionValor.split("=");

							ACTIONS accion = ACTIONS.fromString(arrayAccionValor[0].trim()); // Acción
							double valor = Double.parseDouble(arrayAccionValor[1].trim()); // Valor Q

							acciones.put(accion, valor); // Guardar acción y valor Q
						}
					}

					qTable.put(estado, acciones);
				}
			}
		}

		System.out.println("QTable cargada correctamente desde: " + filePath);
	}
	
	public static void muestraContadorEstados() {
		
		System.out.println("\nContador de estados:\n");
		
		for (Map.Entry<Long, Integer> entry : contadorEstados.entrySet()) {
            System.out.println(entry.getKey() + " --> " + entry.getValue());
        }
	}
	
	public static void exportarContadorEstadosCSV(String filename) {

		try (PrintWriter writer = new PrintWriter(new File(filename))) {

			StringBuilder sb = new StringBuilder();

			sb.append("Estado,Contador\n");

			for (Map.Entry<Long, Integer> entry : contadorEstados.entrySet()) {
				sb.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
			}

			writer.write(sb.toString());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
