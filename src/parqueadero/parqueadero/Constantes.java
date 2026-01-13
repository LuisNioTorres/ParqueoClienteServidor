package parqueadero;

public abstract class Constantes {
    // *** CONSTANTES SERVIDORSUBASTA ***
    public static final double VALOR_INICIAL = 25.00; // El objeto comienza en 25.00
    public static final long DURACION_MS = 300000; // 5 minutos de subasta base
    public static final long GRACE_PERIOD_MS = 5000; // Últimos 5 segundos para que la puja extienda el tiempo
    public static final long EXTENSION_MS = 5000; // Extensión de 5 segundos por puja
    // *************************

}
