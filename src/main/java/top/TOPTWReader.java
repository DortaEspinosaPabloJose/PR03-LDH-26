package top;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;

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
     * @note Ante un error de entrada/salida lanza una `UncheckedIOException` con la causa original,
     *       de modo que el código llamante decide cómo reaccionar (no finaliza el programa).
     *
     * @param filePath ruta del fichero de la instancia (por ejemplo `resources/c101.txt`).
     * @return instancia del problema lista para usar.
     * @throws UncheckedIOException si el fichero no existe o no puede leerse.
     */
    public static TOPTW readProblem(String filePath) {
        TOPTW problem;
        File instanceFile = new File(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(instanceFile))) {
            String line = ExpositoUtilities.simplifyString(reader.readLine());
            String[] parts = line.split(" ");
            problem = new TOPTW(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]));
            reader.readLine(); // se descarta la segunda línea
            for (int i = 0; i < problem.getPOIs() + 1; i++) {
                line = ExpositoUtilities.simplifyString(reader.readLine());
                parts = line.split(" ");
                problem.setX(i, Double.parseDouble(parts[1]));
                problem.setY(i, Double.parseDouble(parts[2]));
                problem.setServiceTime(i, Double.parseDouble(parts[3]));
                problem.setScore(i, Double.parseDouble(parts[4]));
                if (i == 0) {
                    problem.setReadyTime(i, Double.parseDouble(parts[7]));
                    problem.setDueTime(i, Double.parseDouble(parts[8]));
                } else {
                    problem.setReadyTime(i, Double.parseDouble(parts[8]));
                    problem.setDueTime(i, Double.parseDouble(parts[9]));
                }
            }
            problem.calculateDistanceMatrix();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la instancia: " + filePath, e);
        }
        problem.setMaxTimePerRoute(problem.getDueTime(0));
        return problem;
    }

}