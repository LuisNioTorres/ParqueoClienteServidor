package ejemploSubasta;

import java.net.*;
import java.io.IOException;
import java.util.Date;
import ejemploSubasta.*;

/**
 * Servidor de Subastas. Gestiona la puja, el tiempo restante (deadline)
 * y la concurrencia.
 * 
 * @author M. L. Liu & Gemini
 */
public class ServidorSubasta {

    // *** NUEVAS CONSTANTES ***
    public static final double VALOR_INICIAL = 25.00; // El objeto comienza en 25.00
    public static final long DURACION_MS = 300000; // 5 minutos de subasta base
    public static final long GRACE_PERIOD_MS = 5000; // Últimos 5 segundos para que la puja extienda el tiempo
    public static final long EXTENSION_MS = 5000; // Extensión de 5 segundos por puja
    // *************************

    // Estado global de la subasta (protegido por exclusión mutua)
    public static String producto = "Objeto Raro Coleccionable (Base: $" + VALOR_INICIAL + ")";
    // La puja inicial es el VALOR_INICIAL
    public static volatile double pujaActual = VALOR_INICIAL;
    public static volatile String postorActual = "Nadie (Puja inicial)";

    public static long deadline; // El tiempo final de la subasta

    public static void main(String[] args) {
        int puertoServidor = 7;

        if (args.length == 1)
            puertoServidor = Integer.parseInt(args[0]);

        // Establecer el deadline
        deadline = System.currentTimeMillis() + DURACION_MS;

        try {
            ServerSocket miSocketConexion = new ServerSocket(puertoServidor);
            System.out.println("Servidor de Subasta listo. Producto: " + producto);
            System.out.println("Deadline: " + new Date(deadline));
            System.out.println("Precio inicial: $" + pujaActual);

            while (true) {
                // ⏳ Detener la aceptación de nuevas conexiones después del deadline.
                if (System.currentTimeMillis() > deadline) {
                    System.out.println("\n--- ¡DEADLINE ALCANZADO! Subasta cerrada. ---");
                    break;
                }

                System.out.println("\nEsperando un nuevo postor.");

                Socket socketCliente = miSocketConexion.accept();
                MiSocketStream miSocketDatos = new MiSocketStream(socketCliente);

                System.out.println(
                        "Conexión de postor aceptada desde: " + socketCliente.getInetAddress().getHostAddress());

                // Arranca un hilo (Concurrencia)
                Thread elHilo = new Thread(new HiloServidorSubasta(miSocketDatos));
                elHilo.start();
            }
            miSocketConexion.close();
        } catch (IOException ex) {
            System.err.println("Error de I/O en el servidor: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}