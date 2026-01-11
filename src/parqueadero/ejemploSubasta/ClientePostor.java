package ejemploSubasta;

import java.io.*;
import java.net.SocketException;
import ejemploSubasta.*;

/**
 * Módulo de presentación del Cliente Postor.
 * 
 * @author M. L. Liu & Gemini
 */
public class ClientePostor {
	static final String mensajeFin = ".";

	public static void main(String[] args) {
		InputStreamReader is = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(is);

		MiSocketStream miSocket = null;

		try {
			System.out.println("🤝 Bienvenido al cliente Postor de Subastas.\n" +
					"¿Cuál es el nombre de la máquina servidora?");
			String nombreMaquina = br.readLine();
			if (nombreMaquina.length() == 0)
				nombreMaquina = "localhost";

			System.out.println("¿Cuál es el n° puerto de la máquina servidora?");
			String numPuerto = br.readLine();
			if (numPuerto.length() == 0)
				numPuerto = "7";

			int puerto = Integer.parseInt(numPuerto);

			// Se asume que MiSocketStream tiene constructores públicos (ya corregido)
			miSocket = new MiSocketStream(nombreMaquina, puerto);

			// 1. Lectura del Prompt de Nombre
			String respuesta = miSocket.recibeMensaje();
			System.out.println(respuesta);

			// 2. Envío del Nombre del Postor
			String nombrePostor = br.readLine();
			miSocket.enviaMensaje(nombrePostor);

			// 3. Lectura del Estado Inicial (incluye el tiempo restante)
			System.out.println(miSocket.recibeMensaje());

			boolean hecho = false;
			String mensaje, respuestaSubasta;
			while (!hecho) {
				System.out.println("\n💰 Introduce tu PUJA (solo el número) "
						+ "o un único punto (.) para salir.");
				mensaje = br.readLine();
				if ((mensaje.trim()).equals(mensajeFin)) {
					hecho = true;
					miSocket.enviaMensaje(mensajeFin); // Avisa al servidor que termina
				} else {
					// Envía la puja
					miSocket.enviaMensaje(mensaje);

					// Lee la respuesta del servidor (Éxito/Fallo de la puja)
					respuestaSubasta = miSocket.recibeMensaje();
					System.out.println(respuestaSubasta);

					// Lee el estado actual (Incluye el tiempo restante/ganador)
					respuestaSubasta = miSocket.recibeMensaje();
					System.out.println(respuestaSubasta);
				}
			} // fin de while
		} catch (SocketException ex) {
			System.err.println("Error de conexión: El servidor no está disponible o el puerto es incorrecto.");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (miSocket != null) {
					miSocket.close();
				}
			} catch (IOException e) {
				/* Ignorar error de cierre */ }
		}
	} // fin de main
} // fin de class