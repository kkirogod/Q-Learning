package aa2024.miguelquirogaalu;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.Vector2d;

public class Mundo118 {

	public static boolean visualizar;
	
	public static final int META = 10, MONEDA = 12, MURO = 0, SUELO = 5, ZOMBIE = 8, TIBURON = 9, PLATAFORMA = 3, FUEGO = 11, NUBE = 4;
	
	public static Stack<Long> historialEstados = new Stack<Long>();
	public static Stack<ACTIONS> historialAcciones = new Stack<>();

	private ArrayList<Observation>[][] entorno;
	private Vector2d posicionAvatar;
	private ArrayList<Vector2d> posicionesAvatar;
	public static int bloque;
	private int numFilas;
	private int numColumnas;
	private StateObservation stateObs;
	private Random rnd;
	private int tick;
	
	private int numMonedas;

	private Vector2d posicionMeta;
	private ArrayList<Vector2d> posicionesObjetivos;
	private ArrayList<Vector2d> posicionesEnemigos;
	
	public Mundo118(StateObservation stateObs) {

		this.stateObs = stateObs;

		bloque = stateObs.getBlockSize();
		entorno = stateObs.getObservationGrid();

		numFilas = entorno[0].length;
		numColumnas = entorno.length;

		posicionAvatar = stateObs.getAvatarPosition();

		rnd = new Random(System.currentTimeMillis());
	}

	public void actualizar(StateObservation stateObs) {

		this.stateObs = stateObs;

		tick = stateObs.getGameTick();

		rnd = new Random(System.currentTimeMillis());

		entorno = stateObs.getObservationGrid();
		posicionAvatar = stateObs.getAvatarPosition();

		posicionesAvatar = new ArrayList<Vector2d>();
		posicionesObjetivos = new ArrayList<Vector2d>();
		posicionesEnemigos = new ArrayList<Vector2d>();
		
		numMonedas = 0;

		if (visualizar) {
			System.out.println("\n\n*********************************************\n");
			System.out.println("Tick: " + tick);
			System.out.println("Tamaño bloque: " + bloque);
			System.out.println("Numero filas: " + numFilas);
			System.out.println("Numero de columnas: " + numColumnas);
			System.out.println("Tamaño del entorno: " + (bloque * numColumnas) + "x" + (bloque * numFilas));
			System.out.println("Posicion Avatar [F, C]: [" + coordenada(posicionAvatar.y) + ", "
					+ coordenada(posicionAvatar.x) + "] - (" + posicionAvatar.y + ", " + posicionAvatar.x + ")");
			System.out.println("Orientacion Avatar: " + orientacion() + " - (" + stateObs.getAvatarOrientation() + ")");
			System.out.println("Velocidad del avatar: " + stateObs.getAvatarSpeed());
			System.out.println("Acciones disponibles: " + accionesDisponibles());
			System.out.println("");
		}

		for (int y = 0; y < numFilas; y++) {
			for (int x = 0; x < numColumnas; x++) {

				ArrayList<Observation> contenido = entorno[x][y];
				String celda = null;

				if (x == 0)
					System.out.print(" ");

				if (contenido.size() > 0) {

					for (Observation propiedades : contenido) {

						if (propiedades.position.x == posicionAvatar.x && propiedades.position.y == posicionAvatar.y) {

							posicionesAvatar.add(propiedades.position);
							celda = "X";
							
						} else {
							switch (propiedades.itype) {
							case MURO:
								celda = "#";
								break;
							case SUELO:
								celda = "#";
								break;
							case MONEDA:
								celda = "O";
								posicionesObjetivos.add(propiedades.position);
								numMonedas++;
								break;
							case META:
								celda = "@";
								posicionMeta = propiedades.position;
								posicionesObjetivos.add(propiedades.position);
								break;
							case ZOMBIE:
								celda = "Z";
								posicionesEnemigos.add(propiedades.position);
								break;
							case TIBURON:
								celda = "T";
								posicionesEnemigos.add(propiedades.position);
								break;
							case PLATAFORMA:
								celda = "P";
								break;
							case FUEGO:
								celda = "F";
								break;
							case NUBE:
								celda = "N";
								break;
							default:
								celda = String.valueOf(propiedades.itype);
							}
						}
					}
				} else
					celda = " ";

				celda = String.format("%3s", celda); // para que se vea más espaciado

				if (visualizar)
					System.out.print(celda);
			}
			if (visualizar)
				System.out.println("");
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public ArrayList<Integer> getEntorno() {
		
		ArrayList<Integer> estados = new ArrayList<Integer>();
		int index = 0;
		
		for (int j = -1; j < 2; j++) {
			for (int i = -1; i < 2; i++) {
				
				if (i == 0 && j == 0) // posicion del avatar
					continue;
				
				// si es fuera del mapa
				if ((((int) coordenada(posicionAvatar.y) + j) >= entorno[0].length) || (((int) coordenada(posicionAvatar.y) + j) < 0) || (((int) coordenada(posicionAvatar.x) + i) >= entorno.length) || (((int) coordenada(posicionAvatar.x) + i) < 0)) {
					estados.add(index, 9);
					index++;
					continue;
				}
				
				ArrayList<Observation> contenido = entorno[(int) coordenada(posicionAvatar.x) + i][(int) coordenada(posicionAvatar.y) + j];
		
				estados.add(index, 0);
				
				if (contenido.size() > 0) {
					
					for (int k = 0; k < contenido.size(); k++) {
						
						switch (contenido.get(k).itype) {
						case MURO:
						case SUELO:
						case PLATAFORMA:
							estados.set(index, 1);
							break;
						case MONEDA:
						case META:
							estados.set(index, 2);
							break;
						case ZOMBIE:
						case TIBURON:
							estados.set(index, 3);
							break;
						//case FUEGO:
						//case NUBE:
						}
					}
				}
				
				index++;
			}
		}
		
		return estados;
	}
	
	// Devuelve un ID que identifique al estado actual del mundo
	public long getEstado() {
		
		Vector2d objetivo = objetivoMasCercano();
		
		
		double dx = coordenada(objetivo.x) - coordenada(posicionAvatar.x);
		
		int e1 = 0; // distancia horizontal al obj + cercano
		
		if (dx > 0) {
			if (dx >= 10)
				e1 = 8;
			else if (dx >= 6)
				e1 = 6;
			else if (dx >= 3)
				e1 = 4;
			else
				e1 = 2;
		}
		else if (dx < 0) {
			if (dx <= -10)
				e1 = 7;
			else if (dx <= -6)
				e1 = 5;
			else if (dx <= -3)
				e1 = 3;
			else
				e1 = 1;
		}
		
		/*
		if (dx > 0) {
			if (dx < 3)
				e1 = 2;
		}
		else if (dx <= 0) {
			if (dx > -3)
				e1 = 1;
		}
		*/
		
		
		double dy = coordenada(objetivo.y) - coordenada(posicionAvatar.y);
		
		int e2 = 0; // distancia vertical al obj + cercano
		
		if (dy > 0) {
			if (dy >= 5)
				e2 = 6;
			else if (dy >= 2)
				e2 = 4;
			else
				e2 = 2;
		}
		else if (dy < 0) {
			if (dy <= -5)
				e2 = 5;
			else if (dy <= -2)
				e2 = 3;
			else
				e2 = 1;
		}
		
		/*
		if (dy > 0) {
			if (dy < 2)
				e2 = 2;
		}
		else if (dy <= 0) {
			if (dy > -2)
				e2 = 1;
		}
		*/
		
		
		int e3 = 0; // enemigo cercano
		
		if (posicionesEnemigos.size() > 0) {
			
			//if (distanciaEuclidea((int)coordenada(posicionAvatar.x), (int)coordenada(posicionAvatar.y), (int)coordenada(enemigoMasCercano().x), (int)coordenada(enemigoMasCercano().y)) <= 2.0) e3 = 1;
			
			if (coordenada(distanciaManhattan(enemigoMasCercano())) <= 3.0)
				e3 = 1;
		}
		
		
		int e4 = enAire() == true ? 1 : 0; // en el aire
		
        return Long.parseLong(String.format("%d%d%d%d", e1, e2, e3, e4));
	}
	
	public double recompensa(long estado, long estadoSig, int flag) {
		
		if (flag == 1) // victoria
			return 100;
		else if (flag == -1) // derrota
			return -25;
		else if (flag == 2) // consigo moneda
			return 50;
		
		
		String s = String.format("%04d", estado);
		
		int[] eIni = new int[4];
		
		// Extraemos las partes
		for (int i = 0; i < 4; i++) {
			eIni[i] = Character.getNumericValue(s.charAt(i));
		}
        
        s = String.format("%04d", estadoSig);

        int[] eFin = new int[4];

        // Extraemos las partes
        for (int i = 0; i < 4; i++) {
        	eFin[i] = Character.getNumericValue(s.charAt(i));
		}
        
        double recompensa = 0.0;
        
        // si me acerco al objetivo (probar a premiar más el acercamiento horizontal que el vertical)
        // recompenso mas por acercarse de lo que penalizo por alejarse
        if (((eFin[0] < eIni[0]) && (mismaParidad(eFin[0], eIni[0]))) || ((eFin[1] < eIni[1]) && (mismaParidad(eFin[1], eIni[1]))))
        	recompensa += 10;
        
        if (((eFin[0] > eIni[0]) && (mismaParidad(eFin[0], eIni[0]))) || ((eFin[1] > eIni[1]) && (mismaParidad(eFin[1], eIni[1]))))
        	recompensa += -5;
        
        /*
        if ((eFin[0] > eIni[0] && eIni[0] == 0) || (eFin[1] > eIni[1] && eIni[1] == 0))
        	recompensa += 10;
        
        if ((eFin[0] < eIni[0] && eFin[0] == 0) || (eFin[1] < eIni[1] && eFin[1] == 0))
        	recompensa += -5;
		*/
        
        return recompensa;
    }
	

	public Vector2d objetivoMasCercano() {

		Vector2d posObj = null;
		double distanciaMasCercana = Double.MAX_VALUE;
		
		if (posicionesObjetivos.size() > 1) // quedan monedas aún
			posicionesObjetivos.remove(posicionMeta);

		for (Vector2d pos : posicionesObjetivos) {

			double d = distanciaManhattan(pos);

			if (d < distanciaMasCercana) {
				
				posObj = pos;
				distanciaMasCercana = d;
			}
		}

		return posObj;
	}
	
	public Vector2d enemigoMasCercano() {

		Vector2d posEnemigo = null;
		double distanciaMasCercana = Double.MAX_VALUE;

		for (Vector2d pos : posicionesEnemigos) {

			double d = distanciaManhattan(pos);

			if (d < distanciaMasCercana) {
				posEnemigo = pos;
				distanciaMasCercana = d;
			}
		}

		return posEnemigo;
	}

	public int etiqueta(Vector2d pos) {

		ArrayList<Observation> contenido = entorno[(int) coordenada(pos.x)][(int) coordenada(pos.y)];

		int tipo = 1;

		for (int i = 0; i < contenido.size(); i++) {
			tipo = contenido.get(i).itype;
		}

		return tipo;
	}
	
	
	public boolean enAire() {
		
		boolean enAire = true;
		
		if (((int) coordenada(posicionAvatar.y) + 1) < entorno[0].length) {
			
			ArrayList<Observation> abajo = entorno[(int) coordenada(posicionAvatar.x)][(int) coordenada(posicionAvatar.y) + 1]; // miro la celda de abajo
	
			for (int i = 0; i < abajo.size(); i++) {
				if (abajo.get(i).itype == MURO || abajo.get(i).itype == SUELO || abajo.get(i).itype == PLATAFORMA) {
					enAire = false;
					break;
				}
			}
		}
		
		return enAire;
	}
	
	
	public boolean hayObstaculoArriba(Vector2d obj) {
		
		boolean hayObstaculo = false;
		
		int x = (int) coordenada(posicionAvatar.x);
		int y = (int) coordenada(posicionAvatar.y) - 1;
		
		int yObj = (int) coordenada(obj.y);
		
		while (y > yObj && !hayObstaculo) {
		
			ArrayList<Observation> arriba = entorno[x][y]; // miro arriba
	
			for (int i = 0; i < arriba.size(); i++) {
				if (arriba.get(i).itype == MURO || arriba.get(i).itype == SUELO || arriba.get(i).itype == PLATAFORMA) {
					hayObstaculo = true;
					break;
				}
			}
			
			y--;
		}
		
		return hayObstaculo;
	}

	
	public List<ACTIONS> accionesDisponibles() {

		List<ACTIONS> acciones = new LinkedList<ACTIONS>();
		
		acciones.add(ACTIONS.ACTION_RIGHT);
		acciones.add(ACTIONS.ACTION_LEFT);
		acciones.add(ACTIONS.ACTION_USE);
		/*
		ArrayList<Observation> contenido;

		contenido = entorno[(int) coordenada(posicionAvatar.x) + 1][(int) coordenada(posicionAvatar.y)];

		acciones.add(ACTIONS.ACTION_RIGHT);

		for (int i = 0; i < contenido.size(); i++) {
			if (contenido.get(i).itype == MURO || contenido.get(i).itype == SUELO || contenido.get(i).itype == ZOMBIE)
				acciones.remove(ACTIONS.ACTION_RIGHT);
		}

		contenido = entorno[(int) coordenada(posicionAvatar.x) - 1][(int) coordenada(posicionAvatar.y)];

		acciones.add(ACTIONS.ACTION_LEFT);

		for (int i = 0; i < contenido.size(); i++) {
			if (contenido.get(i).itype == MURO || contenido.get(i).itype == SUELO || contenido.get(i).itype == ZOMBIE)
				acciones.remove(ACTIONS.ACTION_LEFT);
		}

		contenido = entorno[(int) coordenada(posicionAvatar.x)][(int) coordenada(posicionAvatar.y) - 1];

		acciones.add(ACTIONS.ACTION_USE);

		for (int i = 0; i < contenido.size(); i++) {
			if (contenido.get(i).itype == MURO || contenido.get(i).itype == SUELO || contenido.get(i).itype == ZOMBIE)
				acciones.remove(ACTIONS.ACTION_USE);
		}
		
		//if (enAire())
			//acciones.remove(ACTIONS.ACTION_USE);
		
		/*
		 * contenido = entorno[(int) coordenada(posicionAvatar.x)][(int)
		 * coordenada(posicionAvatar.y) + 1];
		 * 
		 * acciones.add(ACTIONS.ACTION_NIL);
		 * 
		 * for (int i = 0; i < contenido.size(); i++) { if (contenido.get(i).itype ==
		 * MURO || contenido.get(i).itype == SUELO) acciones.remove(ACTIONS.ACTION_NIL);
		 * }
		 */

		if (acciones.size() == 0) {
			acciones.add(ACTIONS.ACTION_NIL);
		}
		
		return acciones;
	}
	
	
	public ACTIONS accionAleatoria() {

		List<ACTIONS> acciones = new LinkedList<ACTIONS>();
		acciones.add(ACTIONS.ACTION_LEFT);
		acciones.add(ACTIONS.ACTION_RIGHT);
		acciones.add(ACTIONS.ACTION_USE);
		acciones.add(ACTIONS.ACTION_NIL);

		Random rnd = new Random(System.currentTimeMillis());

		return acciones.get(rnd.nextInt(4));
	}

	public Vector2d proxPosicionConOrientacion(ACTIONS accion) {

		Vector2d proxPosicion = null;

		switch (accion) {
		case ACTION_RIGHT:
			if (orientacion().equals(accion))
				proxPosicion = new Vector2d(posicionAvatar.x + bloque, posicionAvatar.y);
			else
				proxPosicion = new Vector2d(posicionAvatar.x, posicionAvatar.y);
			break;
		case ACTION_LEFT:
			if (orientacion().equals(accion))
				proxPosicion = new Vector2d(posicionAvatar.x - bloque, posicionAvatar.y);
			else
				proxPosicion = new Vector2d(posicionAvatar.x, posicionAvatar.y);
			break;
		case ACTION_NIL:
			proxPosicion = new Vector2d(posicionAvatar.x, posicionAvatar.y);
			break;
		default:
			break;
		}

		return proxPosicion;
	}

	public Vector2d proxPosicionSinOrientacion(ACTIONS accion) {

		Vector2d proxPosicion = null;

		switch (accion) {
		case ACTION_RIGHT:
			proxPosicion = new Vector2d(posicionAvatar.x + bloque, posicionAvatar.y);
			break;
		case ACTION_LEFT:
			proxPosicion = new Vector2d(posicionAvatar.x - bloque, posicionAvatar.y);
			break;
		case ACTION_NIL:
			proxPosicion = new Vector2d(posicionAvatar.x, posicionAvatar.y);
			break;
		default:
			break;
		}

		return proxPosicion;
	}

	public ACTIONS proxAccion(Vector2d salto) {

		if (Math.abs(salto.x - posicionAvatar.x) >= bloque && salto.x < posicionAvatar.x)
			return ACTIONS.ACTION_LEFT;

		if (Math.abs(salto.x - posicionAvatar.x) >= bloque && salto.x > posicionAvatar.x)
			return ACTIONS.ACTION_RIGHT;

		if (Math.abs(salto.y - posicionAvatar.y) >= bloque && salto.y < posicionAvatar.y)
			return ACTIONS.ACTION_USE;

		if (Math.abs(salto.y - posicionAvatar.y) >= bloque && salto.y > posicionAvatar.y)
			return ACTIONS.ACTION_NIL;

		return null;
	}
	
	public ACTIONS proxAccion(Vector2d origen, Vector2d destino) {

		if (Math.abs(destino.x - origen.x) >= bloque && destino.x < origen.x)
			return ACTIONS.ACTION_LEFT;

		if (Math.abs(destino.x - origen.x) >= bloque && destino.x > origen.x)
			return ACTIONS.ACTION_RIGHT;

		if (Math.abs(destino.y - origen.y) >= bloque && destino.y < origen.y)
			return ACTIONS.ACTION_USE;

		if (Math.abs(destino.y - origen.y) >= bloque && destino.y > origen.y)
			return ACTIONS.ACTION_NIL;

		return null;
	}

	public ACTIONS acercarse(Vector2d destino, List<ACTIONS> acciones) {

		ACTIONS accion;

		if (coordenada(destino.x) > coordenada(posicionAvatar.x) && acciones.contains(ACTIONS.ACTION_RIGHT))
			accion = ACTIONS.ACTION_RIGHT;

		else if (coordenada(destino.x) < coordenada(posicionAvatar.x) && acciones.contains(ACTIONS.ACTION_LEFT))
			accion = ACTIONS.ACTION_LEFT;

		else if (coordenada(destino.y) > coordenada(posicionAvatar.y) && acciones.contains(ACTIONS.ACTION_NIL))
			accion = ACTIONS.ACTION_NIL;

		else if (coordenada(destino.y) < coordenada(posicionAvatar.y) && acciones.contains(ACTIONS.ACTION_USE))
			accion = ACTIONS.ACTION_USE;

		else if (acciones.size() > 1)
			accion = acciones.get(rnd.nextInt(acciones.size()));

		else
			accion = acciones.get(0);

		return accion;
	}

	public ACTIONS alejarse(Vector2d pos, List<ACTIONS> acciones) {

		ACTIONS accion;

		if (coordenada(pos.x) > coordenada(posicionAvatar.x) && acciones.contains(ACTIONS.ACTION_RIGHT))
			accion = ACTIONS.ACTION_LEFT;

		else if (coordenada(pos.x) < coordenada(posicionAvatar.x) && acciones.contains(ACTIONS.ACTION_LEFT))
			accion = ACTIONS.ACTION_RIGHT;

		else if (coordenada(pos.y) > coordenada(posicionAvatar.y) && acciones.contains(ACTIONS.ACTION_NIL))
			accion = ACTIONS.ACTION_USE;

		else if (coordenada(pos.y) < coordenada(posicionAvatar.y) && acciones.contains(ACTIONS.ACTION_USE))
			accion = ACTIONS.ACTION_NIL;

		else if (acciones.size() > 1)
			accion = acciones.get(rnd.nextInt(acciones.size()));

		else
			accion = acciones.get(0);

		return accion;
	}

	public ACTIONS orientacion() {
		
		double x = stateObs.getAvatarOrientation().x;
		double y = stateObs.getAvatarOrientation().y;
		
		if (Math.abs(x) > Math.abs(y)) {
			if (x > 0)
				return ACTIONS.ACTION_RIGHT;
			else if (x < 0)
				return ACTIONS.ACTION_LEFT;
		}
		else if (Math.abs(x) < Math.abs(y)) {
			if (y > 0)
				return ACTIONS.ACTION_DOWN;
			else if (y < 0)
				return ACTIONS.ACTION_UP;
		}

		return null;
	}
	
	public ACTIONS orientacionHorizontal() {
		
		double x = stateObs.getAvatarOrientation().x;

		if (x > 0)
			return ACTIONS.ACTION_RIGHT;
		else if (x < 0)
			return ACTIONS.ACTION_LEFT;
		else
			return null;
	}

	public static double coordenada(double xy) {
		return xy / bloque;
	}

	public Vector2d getPosicionAvatar() {
		return posicionAvatar;
	}

	public double distanciaManhattan(Vector2d destino) {

		return Math.abs(posicionAvatar.x - destino.x) + Math.abs(posicionAvatar.y - destino.y);
	}

	public double distanciaManhattan(Vector2d origen, Vector2d destino) {

		return (Math.abs(origen.x - destino.x) + Math.abs(origen.y - destino.y));
	}

	public double distanciaManhattan(double xi, double yi, double xf, double yf) {
		return Math.abs(xi - xf) + Math.abs(yi - yf);
	}
	
	public double distanciaEuclidea(int xi, int yi, int xf, int yf) {

        int diferenciaX = xi - xf;
        int diferenciaY = yi - yf;
        
        return Math.sqrt(diferenciaX * diferenciaX + diferenciaY * diferenciaY);
    }
	
	public boolean mismaParidad(int a, int b) {
	    return ((a % 2) == (b % 2)) || (a == 0) || (b == 0);
	}

	public ArrayList<Vector2d> getPosicionesAvatar() {
		return posicionesAvatar;
	}

	public int getTick() {
		return tick;
	}
	
	public int getNumMonedas() {
		return numMonedas;
	}
}
