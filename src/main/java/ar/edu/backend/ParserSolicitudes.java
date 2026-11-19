package ar.edu.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParserSolicitudes {
    private static final String CABECERA = "idCliente,tipoCliente,ingresosMensuales,totalDeudasActuales,estadoSolicitud";
    // defino nueva cabecera
    private static final String NUEVA_CABECERA = "idCliente,tipoCliente,ingresosMensuales,totalDeudasActuales,estadoSolicitud,nivelMorosidad";

    public ResultadoProceso procesar(Path archivo) throws IOException {

        ArrayList<SolicitudCredito> creditos = new ArrayList<>();
        ArrayList<String> errores = new ArrayList<>();
        ArrayList<String> descartes = new ArrayList<>();
        int leidas = 0;

        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            // me guardo la primera linea
            String primeraLinea = lector.readLine();
            // defino variable de columnas
            int columnasEsperadas;

            // verifico si coincide con el encabezado, si es asi guardo las columnas q espero
            if (CABECERA.equals(primeraLinea)) {
                columnasEsperadas = 5;
            } else if (NUEVA_CABECERA.equals(primeraLinea)) {
                columnasEsperadas = 6;
            } else {
                throw new IllegalArgumentException("encabezado incorrecto");
            }

            String linea;
            while ((linea = lector.readLine()) != null) {
                leidas++;
                int numero = leidas + 1;
                try {
                    String[] tokens = Arrays.stream(linea.split(",", -1)).map(String::strip).toArray(String[]::new);
                    if (tokens.length != columnasEsperadas)
                        throw new IllegalArgumentException("Cantidad de columnas incorrectas");
                    if (tokens[1].equalsIgnoreCase("black")) {
                        descartes.add("Linea %d: BLACK".formatted(numero));
                        continue;
                    }

                    // verifico los estados con la morosidad
                    if (tokens.length == 6) {
                        // borro los espacios del estado y declaro morosidad
                        String estadoLimpio = tokens[4].strip();
                        String morosidad = tokens[5];

                        // si es aprobado o pre aprobado tiene q ser normal o en mora, sino error
                        // si es rechazado solo acepta q sea en mora, otra cosa error
                        if (List.of("APROBADO", "PRE_APROBADO","RECHAZADO").contains(estadoLimpio)) {
                            if (!List.of("NORMAL", "EN_MORA").contains(morosidad)) {
                                throw new IllegalArgumentException("Aprobado o pre aprobado solo pueden recibir, normal o en mora");
                            }

                        } else {
                            throw new IllegalArgumentException("Estado desconocido");
                        }
                    } else {
                        if (!List.of("REGULAR", "PREMIUM").contains(tokens[1]))
                            throw new IllegalArgumentException("estado desconocido: " + tokens[1]);
                    }

                    creditos.add(SolicitudCredito.desdeCampos(tokens));

                } catch (IllegalArgumentException ex) {
                    errores.add("Linea %d: %s".formatted(numero, ex.getMessage()));
                }
            }
        }
        return new ResultadoProceso(creditos, leidas, descartes.size(), errores, descartes);

    }
}
