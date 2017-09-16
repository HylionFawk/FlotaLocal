package tablero;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class Juego {

	/**
	 * Implementa el juego 'Hundir la flota' mediante una interfaz grÃ¡fica (GUI)
	 */

	/** Parametros por defecto de una partida */
	public static final int NUMFILAS=8, NUMCOLUMNAS=8, NUMBARCOS=6;
	public static final String SALIR="Salir", NUEVAPARTIDA="Nueva Partida", SOLUCION="Solucion";

	private GuiTablero guiTablero = null;			// El juego se encarga de crear y modificar la interfaz grÃ¡fica
	private Partida partida = null;                 // Objeto con los datos de la partida en juego
	
	/** Atributos de la partida guardados en el juego para simplificar su implementaciÃ³n */
	private int quedan = NUMBARCOS, disparos = 0;
	

	/**
	 * Programa principal. Crea y lanza un nuevo juego
	 * @param args
	 */
	public static void main(String[] args) {
		Juego juego = new Juego();
		juego.ejecuta();
	} // end main

	/**
	 * Lanza una nueva hebra que crea la primera partida y dibuja la interfaz grafica: tablero
	 */
	private void ejecuta() {
		// Instancia la primera partida
		partida = new Partida(NUMFILAS, NUMCOLUMNAS, NUMBARCOS);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				guiTablero = new GuiTablero(NUMFILAS, NUMCOLUMNAS);
				guiTablero.dibujaTablero();
			}
		});
	} // end ejecuta

	/******************************************************************************************/
	/*********************  CLASE INTERNA GuiTablero   ****************************************/
	/******************************************************************************************/
	private class GuiTablero {

		private int numFilas, numColumnas;

		private JFrame frame = null;        // Tablero de juego
		private JLabel estado = null;       // Texto en el panel de estado
		private JButton buttons[][] = null; // Botones asociados a las casillas de la partida

		/**
         * Constructor de una tablero dadas sus dimensiones
         */
		GuiTablero(int numFilas, int numColumnas) {
			this.numFilas = numFilas;
			this.numColumnas = numColumnas;
			frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		}

		/**
		 * Dibuja el tablero de juego y crea la partida inicial
		 */
		public void dibujaTablero() {
			anyadeMenu();
			anyadeGrid(numFilas, numColumnas);		
			anyadePanelEstado("Intentos: " + disparos + "    Barcos restantes: " + quedan);		
			frame.setSize(300, 300);
			frame.setVisible(true);	
		} // end dibujaTablero

		/**
		 * Anyade el menu de opciones del juego y le asocia un escuchador
		 */
		private void anyadeMenu() {
			Container panel= frame.getContentPane();
			JMenuBar menuBar= new JMenuBar();
			JMenu menu= new JMenu("Opciones");
			menuBar.add(menu);
			
			MenuListener listenerMenu= new MenuListener();
			
			JMenuItem salir= new JMenuItem(SALIR);
			salir.setActionCommand(SALIR);
			salir.addActionListener(listenerMenu);
			
			JMenuItem nuevaPartida= new JMenuItem(NUEVAPARTIDA);
			nuevaPartida.setActionCommand(NUEVAPARTIDA);
			nuevaPartida.addActionListener(listenerMenu);
			
			JMenuItem solucion= new JMenuItem(SOLUCION);
			solucion.setActionCommand(SOLUCION);
			solucion.addActionListener(listenerMenu);
			
			menu.add(salir);
			menu.add(nuevaPartida);
			menu.add(solucion);
			
			panel.add(menuBar, BorderLayout.NORTH);
			
		} // end anyadeMenu

		/**
		 * Anyade el panel con las casillas del mar y sus etiquetas.
		 * Cada casilla sera un boton con su correspondiente escuchador
		 * @param nf	numero de filas
		 * @param nc	numero de columnas
		 */
		private void anyadeGrid(int nf, int nc) {
			buttons = new JButton[nf][nc];
          		JPanel grid = new JPanel();
            		grid.setLayout(new GridLayout(nf+1, nc+1));
            		JButton boton;
            		JLabel label;
            
		    for (int fila=0; fila<=nf; fila++){
			for (int col=0; col<=nc+1; col++){
				if(fila==0 && (col==0 || col==nc+1)) { //Pone espacio en blanco
					grid.add(new JLabel());
				} else if (fila==0 && (col!=0 || col!=nc+1)){ //Pone numeros
					label= new JLabel(Integer.toString(col));
					grid.add(label);
				} else if(fila>=1 && (col==0 || col==nc+1)) { //Pone el nombre de las columnas en las columnas 0 y �ltima
					char letra= (char) (fila+64);
					label= new JLabel(Character.toString(letra));
					grid.add(label);
				} else if (fila>0 && col > 0 && col< nc+1){
					boton = new JButton();
					boton.putClientProperty("Fila",fila-1);
					boton.putClientProperty("Columna",col-1);
					boton.addActionListener(new ButtonListener());
					grid.add(boton); 
					buttons[fila-1][col-1] = boton;
				}
			}
		    }

			frame.getContentPane().add(grid, BorderLayout.CENTER);
		} // end anyadeGrid

		/**
		 * Anyade el panel de estado al tablero
		 * @param cadena	cadena inicial del panel de estado
		 */
		private void anyadePanelEstado(String cadena) {	
			JPanel panelEstado = new JPanel();
			estado = new JLabel(cadena);
			panelEstado.add(estado);
			// El panel de estado queda en la posiciÃ³n SOUTH del frame
			frame.getContentPane().add(panelEstado, BorderLayout.SOUTH);
		} // end anyadePanel Estado

		/**
		 * Cambia la cadena mostrada en el panel de estado
		 * @param cadenaEstado	nuevo estado
		 */
		public void cambiaEstado(String cadenaEstado) {
			estado.setText(cadenaEstado);
		} // end cambiaEstado

		/**
		 * Muestra la solucion de la partida y marca la partida como finalizada
		 */
		public void muestraSolucion() {
			
			//Primero se pintan todos los botones de azul
			for (int fila=0; fila<NUMFILAS; fila++) {
				for (int col=0; col<NUMCOLUMNAS; col++) {
					pintaBoton(buttons[fila][col], Color.CYAN);
					buttons[fila][col].removeActionListener( buttons[fila][col].getActionListeners()[0] ); //Remueve el action
				}
			}
			
			//Ahora se pintan solo los botones que sean barcos de rojo
			for(String idBarco : partida.getSolucion()) {
				pintaBarcoHundido(idBarco);
			}
			
            partida.getSolucion();
		} // end muestraSolucion


		/**
		 * Pinta un barco como hundido en el tablero
		 * @param cadenaBarco	cadena con los datos del barco codifificados como
		 *                      "filaInicial#columnaInicial#orientacion#tamanyo"
		 */
		public void pintaBarcoHundido(String cadenaBarco) {
			quedan--;
			String[] datos = cadenaBarco.split("#");
            int filaIni = Integer.parseInt(datos[0]);
            int colIni = Integer.parseInt(datos[1]);
            char orientacion =  datos[2].charAt(0);
            int tamanyo = Integer.parseInt(datos[3]);
            
<<<<<<< HEAD
            JButton b;
            switch (orientacion){
            case "H":
            	for (int i=colIni; i<colIni+tamanyo ;i++){
            		b=buttons[filaIni][i];
            		pintaBoton(b,Color.RED);
            	}
            	break;
            case "V":
            	for (int i=filaIni; i<filaIni+tamanyo; i++){
            		b=buttons[i][colIni];
            		pintaBoton(b,Color.RED);
            	}
            	break;
            }
=======
            JButton boton;
            for(int i=0; i<tamanyo; i++) {
            	
    			if (orientacion=='H') {
    				boton= buttons[filaIni][colIni+i];
    			} else {
    				boton= buttons[filaIni+i][colIni];
    			}
    			pintaBoton(boton, Color.RED);
    		}
    		quedan--;
            
>>>>>>> 7aa859b2cceaedbdb5e56b342b39df69bd77b0d9
		} // end pintaBarcoHundido

		/**
		 * Pinta un botÃ³n de un color dado
		 * @param b			boton a pintar
		 * @param color		color a usar
		 */
		public void pintaBoton(JButton b, Color color) {
			b.setBackground(color);
			// El siguiente cÃ³digo solo es necesario en Mac OS X
			b.setOpaque(true);
			b.setBorderPainted(false);
		} // end pintaBoton

		/**
		 * Limpia las casillas del tablero pintÃ¡ndolas del gris por defecto
		 */
		public void limpiaTablero() {
			for (int i = 0; i < numFilas; i++) {
				for (int j = 0; j < numColumnas; j++) {
					buttons[i][j].setBackground(null);
					buttons[i][j].setOpaque(true);
					buttons[i][j].setBorderPainted(true);
				}
			}
		} // end limpiaTablero

		/**
		 * 	Destruye y libera la memoria de todos los componentes del frame
		 */
		public void liberaRecursos() {
			frame.dispose();
		} // end liberaRecursos


	} // end class GuiTablero

	/******************************************************************************************/
	/*********************  CLASE INTERNA MenuListener ****************************************/
	/******************************************************************************************/

	/**
	 * Clase interna que escucha el menu de Opciones del tablero
	 * 
	 */
	private class MenuListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
            	case SALIR:
            		System.exit(0);
            		break;
            	case NUEVAPARTIDA:
            		guiTablero.limpiaTablero();
            		partida= new Partida(NUMFILAS, NUMCOLUMNAS, NUMBARCOS);
            		break;
            	case SOLUCION:
            		guiTablero.muestraSolucion();
            		break;
            }
		} // end actionPerformed

	} // end class MenuListener



	/******************************************************************************************/
	/*********************  CLASE INTERNA ButtonListener **************************************/
	/******************************************************************************************/
	/**
	 * Clase interna que escucha cada uno de los botones del tablero
	 * Para poder identificar el boton que ha generado el evento se pueden usar las propiedades
	 * de los componentes, apoyandose en los metodos putClientProperty y getClientProperty
	 */
	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JButton boton = (JButton)e.getSource();
			int fila = (int) boton.getClientProperty("Fila");
			int columna = (int) boton.getClientProperty("Columna");
			int res = partida.pruebaCasilla(fila, columna);
			disparos++;
			
			switch (res){
			case -1:
				guiTablero.pintaBoton(boton , Color.CYAN);
				break;
			case -2:
				guiTablero.pintaBoton(boton , Color.ORANGE);
				
				break;
<<<<<<< HEAD
			case -3:
				//guiTablero.pintaBarcoHundido();
				break;				
			}
			guiTablero.cambiaEstado("Intentos: " + disparos + "    Barcos restantes: " + quedan);
			
=======
			default: 
				if(res>=0)
					guiTablero.pintaBarcoHundido(partida.getBarco(res));
				break;
			}
			boton.removeActionListener(this); //Se elimina el boton del listener para que no suba el número de intentos al repetir casilla
			guiTablero.cambiaEstado("Intentos: " + ++disparos + "    Barcos restantes: " + quedan);
>>>>>>> 7aa859b2cceaedbdb5e56b342b39df69bd77b0d9
        } // end actionPerformed

	} // end class ButtonListener



} // end class Juego
