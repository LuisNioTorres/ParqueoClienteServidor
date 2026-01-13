package parqueadero;

import java.io.IOException;
import java.net.ServerSocket;

public class ServidorParqueadero {
    public static void main(String[] args) {
        int puertoServidor = 1000;

        if (args.length > 0)
            puertoServidor = Integer.parseInt(args[0]);

        try {
            ServerSocket socketConexion = new ServerSocket(puertoServidor);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
