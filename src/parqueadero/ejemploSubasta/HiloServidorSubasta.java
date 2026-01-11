package ejemploSubasta;

import java.io.IOException;
import ejemploSubasta.*;;

/**
 * Hilo para manejar una sesión de postor individual. Implementa Concurrencia
 * y Exclusión Mutua para la puja, y la Regla de Extensión de Último Minuto.
 * 
 * @author M. L. Liu & Gemini
 */
public class HiloServidorSubasta implements Runnable {

    private MiSocketStream miSocketDatos;
    private String nombrePostor;

    public HiloServidorSubasta(MiSocketStream socketDatos) {
        this.miSocketDatos = socketDatos;
    }

    private String getEstadoSubasta() {
        long tiempoRestante = ServidorSubasta.deadline - System.currentTimeMillis();
        String estado;
        if (tiempoRestante <= 0) {
            estado = "--- ¡FINALIZADA! --- GANADOR: " + ServidorSubasta.postorActual + " con $"
                    + ServidorSubasta.pujaActual;
        } else {
            long minutos = tiempoRestante / 60000;
            long segundos = (tiempoRestante % 60000) / 1000;

            estado = String.format("Tiempo Restante: %d min, %d s | Puja actual: $%.2f por %s",
                    minutos, segundos, ServidorSubasta.pujaActual, ServidorSubasta.postorActual);
        }
        return "ESTADO: " + estado;
    }

    @Override
    public void run() {
        try {
            miSocketDatos.enviaMensaje("Bienvenido. Introduce tu nombre:");
            nombrePostor = miSocketDatos.recibeMensaje();
            if (nombrePostor == null)
                return;
            System.out.println("Postor " + nombrePostor + " se ha conectado.");

            miSocketDatos.enviaMensaje(getEstadoSubasta());

            String linea;
            while ((linea = miSocketDatos.recibeMensaje()) != null) {
                if (linea.equals(".")) {
                    break;
                }

                if (System.currentTimeMillis() > ServidorSubasta.deadline) {
                    miSocketDatos.enviaMensaje("SUBASSTA CERRADA: El tiempo ha terminado.");
                    miSocketDatos.enviaMensaje(getEstadoSubasta());
                    continue;
                }

                try {
                    double nuevaPuja = Double.parseDouble(linea.trim());

                    // Bloque Sincronizado para Exclusión Mutua
                    synchronized (ServidorSubasta.class) {
                        if (nuevaPuja > ServidorSubasta.pujaActual) {

                            // --- Lógica de Extensión de Último Minuto ---
                            long tiempoRestante = ServidorSubasta.deadline - System.currentTimeMillis();

                            // Si quedan menos del GRACE_PERIOD_MS y la subasta no ha terminado...
                            if (tiempoRestante < ServidorSubasta.GRACE_PERIOD_MS && tiempoRestante > 0) {
                                ServidorSubasta.deadline += ServidorSubasta.EXTENSION_MS;
                                String extensionMsg = String.format(
                                        "--- ¡PUJA DE ÚLTIMO MINUTO! Subasta extendida por %d segundos. ---",
                                        ServidorSubasta.EXTENSION_MS / 1000);
                                System.out.println(extensionMsg);
                                miSocketDatos.enviaMensaje(extensionMsg);
                            }
                            // ------------------------------------------

                            ServidorSubasta.pujaActual = nuevaPuja;
                            ServidorSubasta.postorActual = nombrePostor;

                            String exito = String.format("PUJA ACEPTADA: Nuevo precio por %s: $%.2f por %s",
                                    ServidorSubasta.producto, ServidorSubasta.pujaActual, ServidorSubasta.postorActual);
                            miSocketDatos.enviaMensaje(exito);
                            System.out.println(exito); // Log en el servidor
                        } else {
                            String fallo = String.format(
                                    "PUJA RECHAZADA: $%.2f no supera la puja actual de $%.2f por %s",
                                    nuevaPuja, ServidorSubasta.pujaActual, ServidorSubasta.postorActual);
                            miSocketDatos.enviaMensaje(fallo);
                        }
                        // Informar el estado final de esta ronda
                        miSocketDatos.enviaMensaje(getEstadoSubasta());
                    } // Fin de synchronized (Exclusión Mutua)
                } catch (NumberFormatException e) {
                    miSocketDatos.enviaMensaje("Error: Introduce un valor numérico para pujar. O '.' para salir.");
                    miSocketDatos.enviaMensaje(getEstadoSubasta());
                }
            }
        } catch (IOException ex) {
            System.err.println("Postor " + (nombrePostor != null ? nombrePostor : "Desconocido")
                    + " se desconectó inesperadamente.");
        } finally {
            try {
                if (nombrePostor != null) {
                    System.out.println("Postor " + nombrePostor + " se ha desconectado.");
                }
                miSocketDatos.close();
            } catch (IOException e) {
                /* Ignorar error de cierre */ }
        }
    }
}