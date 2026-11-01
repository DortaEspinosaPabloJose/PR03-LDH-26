package top;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import exposito.utilities.ExpositoUtilities;

/**
 * Lector de instancias del TOPTW desde fichero de texto.
 *
 * Interpreta los ficheros del banco de pruebas (`c1xx`, `r1xx`, `rc1xx`) y construye un objeto
 * `TOPTW` con todos sus datos y la matriz de distancias calculada.
 *
 * ### Formato esperado
 * Los campos se separan por espacios o tabuladores (se normalizan con
 * `ExpositoUtilities.simplifyString()`):
 * 1. **Primera línea:** cabecera. El campo 1 (índice 1) es el número de vehículos y el
 *    campo 2 el número de nodos (POIs).
 * 2. **Segunda línea:** se descarta.
 * 3. **Líneas siguientes:** `nodes + 1` líneas, una por nodo (el nodo 0 es el depósito), con
 *    estas columnas (índice de campo):
 *
 * | Campo | Contenido                                                |
 * |:-----:|----------------------------------------------------------|
 * | 1     | Coordenada X                                             |
 * | 2     | Coordenada Y                                             |
 * | 3     | Tiempo de servicio                                       |
 * | 4     | Puntuación                                               |
 * | 7, 8  | Apertura y cierre de la ventana (sólo en el depósito)    |
 * | 8, 9  | Apertura y cierre de la ventana (resto de nodos)         |
 *
 * @see TOPTW
 */
public class TOPTWReader {

    /**
     * Lee una instancia del problema desde un fichero.
     *
     * Tras cargar los nodos calcula la matriz de distancias y fija el tiempo máximo por ruta
     * igual al cierre de la ventana de tiempo del depósito.
     *
     * @note Ante un error de entrada/salida muestra el error por la salida de error estándar y
     *       **finaliza el programa** (`System.exit(0)`).
     *
     * @param filePath ruta del fichero de la instancia (por ejemplo `resources/c101.txt`).
     * @return instancia del problema lista para usar.
     */
    public static TOPTW readProblem(String filePath) {
        TOPTW problem = null;
        BufferedReader reader = null;
        try {
            File instaceFile = new File(filePath);
            reader = new BufferedReader(new FileReader(instaceFile));
            String line = reader.readLine();
            line = ExpositoUtilities.simplifyString(line);
            String[] parts =line.split(" ");
            problem = new TOPTW(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]));
            line = reader.readLine();
            line = null; parts = null;
            for (int i = 0; i < problem.getPOIs()+1; i++) {
                line = reader.readLine();
                line = ExpositoUtilities.simplifyString(line);
                parts = line.split(" ");
                problem.setX(i, Double.parseDouble(parts[1]));
                problem.setY(i, Double.parseDouble(parts[2]));
                problem.setServiceTime(i, Double.parseDouble(parts[3]));
                problem.setScore(i, Double.parseDouble(parts[4]));
                if(i==0) {
                    problem.setReadyTime(i, Double.parseDouble(parts[7]));
                    problem.setDueTime(i, Double.parseDouble(parts[8]));
                }
                else {
                    problem.setReadyTime(i, Double.parseDouble(parts[8]));
                    problem.setDueTime(i, Double.parseDouble(parts[9]));
                }
                line = null; parts = null;
            }
            problem.calculateDistanceMatrix();
        } catch (IOException e) {
            System.err.println(e);
            System.exit(0);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    System.err.println(ex);
                    System.exit(0);
                }
            }
        }
        problem.setMaxTimePerRoute(problem.getDueTime(0));
        return problem;
    }

}