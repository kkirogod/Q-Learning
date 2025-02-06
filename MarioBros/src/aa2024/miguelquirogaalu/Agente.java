
package aa2024.miguelquirogaalu;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agente extends AbstractPlayer {

	private Mundo118 mundo;
	
	private long estadoAnterior;
	private long estadoActual;
	
	private int numMonedasAnterior;
	private int numMonedasActual;
	
	private ACTIONS accion;
	
	public static boolean visualizar;	
	
	public Agente(StateObservation stateObs, ElapsedCpuTimer elpasedTimer) {
		
		mundo = new Mundo118(stateObs);

		QLearning.inicializar(mundo);
		
		Mundo118.historialEstados.clear();
		Mundo118.historialAcciones.clear();
		
		mundo.actualizar(stateObs);
		estadoActual = mundo.getEstado();
		numMonedasActual = mundo.getNumMonedas();
		
		accion = null;
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elpasedTimer) {

		estadoAnterior = estadoActual;
		numMonedasAnterior = numMonedasActual;
		
		mundo.actualizar(stateObs);
		
		estadoActual = mundo.getEstado();
		numMonedasActual = mundo.getNumMonedas();
		
		//QLearning.registrarEstado(estadoActual); // quitar para TEST
		
		Mundo118.historialEstados.push(estadoActual);
		
		if (visualizar) {
			System.out.println("Estado ant: " + String.format("%04d", estadoAnterior));
			System.out.println("Estado act: " + String.format("%04d", estadoActual));
		}

		if (accion != null) {
			
			Mundo118.historialAcciones.push(accion);
			
			int flag = 0;
			
			if (numMonedasAnterior != numMonedasActual) // he conseguido alguna moneda
				flag = 2;
				
			//QLearning.actualizaTablaQ(estadoAnterior, accion, estadoActual, flag); // quitar para TEST
		}

		accion = QLearning.eligeMejorAccion(estadoActual); // para TEST
		//accion = QLearning.eligeAccion(estadoActual);

		if (visualizar) {
			System.out.println("\nACCION: " + accion);
			//QLearning.showQTable();
		}
		
		return accion;
	}
}