package exposito.utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Colección de utilidades estáticas de propósito general.
 *
 * Agrupa funciones auxiliares usadas por el resto del proyecto:
 * - **Procesado de cadenas** (`simplifyString()`, `isInteger()`, `isDouble()`).
 * - **Formateo de texto en columnas** para informes por consola (`getFormat()` y sus
 *   sobrecargas), con alineación a derecha o izquierda y números con punto decimal.
 * - **Ficheros** (`writeTextToFile()`).
 * - **Matrices y grafos** (`multiplyMatrices()`, `isAcyclic()`, `thereIsPath()`).
 *
 * Todos los métodos son estáticos; no es necesario instanciar la clase.
 */
public class ExpositoUtilities {

    /** Ancho de columna por defecto, en caracteres. */
    public static final int DEFAULT_COLUMN_WIDTH = 10;
    /** Constante de alineación a la izquierda. */
    public static final int ALIGNMENT_LEFT = 1;
    /** Constante de alineación a la derecha. */
    public static final int ALIGNMENT_RIGHT = 2;

    /**
     * Busca la primera aparición de un elemento en un vector.
     *
     * @param vector  vector donde buscar.
     * @param element valor buscado.
     * @return índice de la primera coincidencia, o -1 si no aparece.
     */
    private static int getFirstAppearance(int[] vector, int element) {
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] == element) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Normaliza los espacios de una cadena.
     *
     * Sustituye los tabuladores por espacios, colapsa los espacios consecutivos en uno solo y
     * elimina los espacios iniciales y finales. Útil para trocear líneas con `split(" ")`.
     *
     * @param string cadena de entrada (no puede ser `null`).
     * @return cadena con los espacios simplificados.
     */
    public static String simplifyString(String string) {
        string = string.replaceAll("\t", " ");
        for (int i = 0; i < 50; i++) {
            string = string.replaceAll("  ", " ");
        }
        string = string.trim();
        return string;
    }

    /**
     * Multiplica dos matrices de números reales.
     *
     * @param a matriz de `m x n`.
     * @param b matriz de `n x p`.
     * @return matriz producto de `m x p`; una matriz vacía (`0 x 0`) si `a` no tiene filas, o
     *         `null` si las dimensiones no son compatibles (columnas de `a` distintas de filas de `b`).
     */
    public static double[][] multiplyMatrices(double a[][], double b[][]) {
        if (a.length == 0) {
            return new double[0][0];
        }
        if (a[0].length != b.length) {
            return null;
        }
        int n = a[0].length;
        int m = a.length;
        int p = b[0].length;
        double ans[][] = new double[m][p];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                for (int k = 0; k < n; k++) {
                    ans[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return ans;
    }

    /**
     * Escribe un texto en un fichero, sobrescribiendo su contenido si ya existe.
     *
     * @param file ruta del fichero de destino.
     * @param text texto que se escribirá.
     * @throws IOException si no se puede crear o escribir el fichero.
     */
    public static void writeTextToFile(String file, String text) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(text);
            writer.flush();
        }
    }

    /**
     * Da formato a una cadena si representa un número real.
     *
     * Si la cadena es un número entero se devuelve sin cambios. Si es un número real se
     * reformatea con tres decimales y punto decimal (ver `getFormat(double)`). En cualquier otro
     * caso se devuelve tal cual.
     *
     * @param string cadena a formatear.
     * @return cadena formateada o la original si no es un real.
     */
    public static String getFormat(String string) {
        if (!ExpositoUtilities.isInteger(string)) {
            if (ExpositoUtilities.isDouble(string)) {
                double value = Double.parseDouble(string);
                string = ExpositoUtilities.getFormat(value);
            }
        }
        return string;
    }

    /**
     * Formatea un número real con tres decimales y punto como separador decimal.
     *
     * @param value valor a formatear.
     * @return texto con el formato `0.000`.
     */
    public static String getFormat(double value) {
        DecimalFormat decimalFormatter = new DecimalFormat("0.000");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        decimalFormatter.setDecimalFormatSymbols(symbols);
        return decimalFormatter.format(value);
    }

    /**
     * Formatea un número real con un número dado de decimales y punto decimal.
     *
     * @param value valor a formatear.
     * @param zeros número de decimales a mostrar.
     * @return texto con el valor formateado.
     */
    public static String getFormat(double value, int zeros) {
        String format = "0.";
        for (int i = 0; i < zeros; i++) {
            format += "0";
        }
        DecimalFormat decimalFormatter = new DecimalFormat(format);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        decimalFormatter.setDecimalFormatSymbols(symbols);
        return decimalFormatter.format(value);
    }

    /**
     * Ajusta una cadena a un ancho fijo, alineada a la derecha.
     *
     * @param string cadena a formatear.
     * @param width  ancho de columna en caracteres.
     * @return cadena rellena con espacios a la izquierda hasta alcanzar `width`.
     */
    public static String getFormat(String string, int width) {
        return ExpositoUtilities.getFormat(string, width, ExpositoUtilities.ALIGNMENT_RIGHT);
    }

    /**
     * Ajusta una cadena a un ancho fijo con la alineación indicada.
     *
     * @param string    cadena a formatear.
     * @param width     ancho de columna en caracteres.
     * @param alignment `ALIGNMENT_LEFT` para alinear a la izquierda; cualquier otro valor
     *                  (normalmente `ALIGNMENT_RIGHT`) alinea a la derecha.
     * @return cadena rellena con espacios hasta alcanzar `width`.
     */
    public static String getFormat(String string, int width, int alignment) {
        String format = "";
        if (alignment == ExpositoUtilities.ALIGNMENT_LEFT) {
            format = "%-" + width + "s";
        } else {
            format = "%" + 1 + "$" + width + "s";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        String[] data = new String[]{string};
        return String.format(format, (Object[]) data);
    }

    /**
     * Formatea una lista de cadenas como una fila de columnas del mismo ancho.
     *
     * Cada elemento numérico real se reformatea con `getFormat(String)` y todos se alinean a la
     * derecha.
     *
     * @param strings elementos de la fila.
     * @param width   ancho de cada columna.
     * @return fila de texto con todas las columnas concatenadas.
     */
    public static String getFormat(ArrayList<String> strings, int width) {
        String format = "";
        for (int i = 0; i < strings.size(); i++) {
            format += "%" + (i + 1) + "$" + width + "s";
        }
        String[] data = new String[strings.size()];
        for (int t = 0; t < strings.size(); t++) {
            data[t] = "" + ExpositoUtilities.getFormat(strings.get(t));
        }
        return String.format(format, (Object[]) data);
    }

    /**
     * Formatea una lista de enteros como una fila de columnas de ancho `DEFAULT_COLUMN_WIDTH`.
     *
     * @param strings valores enteros de la fila.
     * @return fila de texto con todas las columnas concatenadas, alineadas a la derecha.
     */
    public static String getFormat(ArrayList<Integer> strings) {
        String format = "";
        for (int i = 0; i < strings.size(); i++) {
            format += "%" + (i + 1) + "$" + DEFAULT_COLUMN_WIDTH + "s";
        }
        Integer[] data = new Integer[strings.size()];
        for (int t = 0; t < strings.size(); t++) {
            data[t] = strings.get(t);
        }
        return String.format(format, (Object[]) data);
    }

    /**
     * Formatea un vector de cadenas como una fila de columnas del mismo ancho, alineadas a la derecha.
     *
     * @param strings elementos de la fila.
     * @param width   ancho común de todas las columnas.
     * @return fila de texto con todas las columnas concatenadas.
     */
    public static String getFormat(String[] strings, int width) {
        int[] alignment = new int[strings.length];
        Arrays.fill(alignment, ExpositoUtilities.ALIGNMENT_RIGHT);
        int[] widths = new int[strings.length];
        Arrays.fill(widths, width);
        return ExpositoUtilities.getFormat(strings, widths, alignment);
    }

    /**
     * Formatea un vector de cadenas como una fila con columnas de ancho `DEFAULT_COLUMN_WIDTH`.
     *
     * @param strings elementos de la fila.
     * @return fila de texto con todas las columnas concatenadas, alineadas a la derecha.
     */
    public static String getFormat(String[] strings) {
        int[] alignment = new int[strings.length];
        Arrays.fill(alignment, ExpositoUtilities.ALIGNMENT_RIGHT);
        int[] widths = new int[strings.length];
        Arrays.fill(widths, ExpositoUtilities.DEFAULT_COLUMN_WIDTH);
        return ExpositoUtilities.getFormat(strings, widths, alignment);
    }

    /**
     * Formatea un vector de cadenas como una fila con un ancho distinto por columna, alineadas a la derecha.
     *
     * @param strings elementos de la fila.
     * @param width   ancho de cada columna (mismo tamaño que `strings`).
     * @return fila de texto con todas las columnas concatenadas.
     */
    public static String getFormat(String[] strings, int[] width) {
        int[] alignment = new int[strings.length];
        Arrays.fill(alignment, ExpositoUtilities.ALIGNMENT_RIGHT);
        return ExpositoUtilities.getFormat(strings, width, alignment);
    }

    /**
     * Formatea un vector de cadenas como una fila con ancho y alineación configurables por columna.
     *
     * Los elementos que representan números reales se reformatean con tres decimales
     * (`getFormat(String)`).
     *
     * @param strings   elementos de la fila.
     * @param width     ancho de cada columna (mismo tamaño que `strings`).
     * @param alignment alineación de cada columna (`ALIGNMENT_LEFT` o `ALIGNMENT_RIGHT`).
     * @return fila de texto con todas las columnas concatenadas.
     */
    public static String getFormat(String[] strings, int[] width, int[] alignment) {
        String format = "";
        for (int i = 0; i < strings.length; i++) {
            if (alignment[i] == ExpositoUtilities.ALIGNMENT_LEFT) {
                format += "%" + (i + 1) + "$-" + width[i] + "s";
            } else {
                format += "%" + (i + 1) + "$" + width[i] + "s";
            }
        }
        String[] data = new String[strings.length];
        for (int t = 0; t < strings.length; t++) {
            data[t] = "" + ExpositoUtilities.getFormat(strings[t]);
        }
        return String.format(format, (Object[]) data);
    }

    /**
     * Comprueba si una cadena representa un número entero.
     *
     * @param str cadena a comprobar.
     * @return `true` si `Integer.parseInt()` puede interpretarla.
     */
    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Comprueba si una cadena representa un número real.
     *
     * @param str cadena a comprobar.
     * @return `true` si `Double.parseDouble()` puede interpretarla.
     */
    public static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Comprueba si un grafo dirigido no contiene ciclos.
     *
     * El grafo se describe con una matriz de adyacencia: la posición `[i][j]` vale
     * `Integer.MAX_VALUE` cuando no existe arco de `i` a `j`. Se asume que los nodos `0` y
     * `length - 1` son nodos ficticios de inicio y fin, de modo que sólo se comprueban los
     * nodos reales `1 .. length - 2`.
     *
     * @param distanceMatrix matriz de adyacencia/costes del grafo.
     * @return `true` si ningún nodo real pertenece a un ciclo.
     * @see #thereIsPath(int[][], int)
     */
    public static boolean isAcyclic(int[][] distanceMatrix) {
        int numRealTasks = distanceMatrix.length - 2;
        int node = 1;
        boolean acyclic = true;
        while (acyclic && node <= numRealTasks) {
            if (ExpositoUtilities.thereIsPath(distanceMatrix, node)) {
                return false;
            }
            node++;
        }
        return true;
    }

    /**
     * Comprueba si, partiendo de un nodo, se puede volver a él (ciclo que pasa por el nodo).
     *
     * Realiza una exploración del grafo a partir de `node` siguiendo los arcos existentes
     * (distintos de `Integer.MAX_VALUE`) y visitando cada nodo una sola vez.
     *
     * @param distanceMatrix matriz de adyacencia/costes del grafo.
     * @param node           nodo de partida.
     * @return `true` si existe un camino no vacío que empieza y termina en `node`.
     */
    public static boolean thereIsPath(int[][] distanceMatrix, int node) {
        HashSet<Integer> visits = new HashSet<>();
        HashSet<Integer> noVisits = new HashSet<>();
        for (int i = 0; i < distanceMatrix.length; i++) {
            if (i != node) {
                noVisits.add(i);
            }
        }
        visits.add(node);
        while (!visits.isEmpty()) {
            Iterator<Integer> it = visits.iterator();
            int toCheck = it.next();
            visits.remove(toCheck);
            for (int i = 0; i < distanceMatrix.length; i++) {
                if (toCheck != i && distanceMatrix[toCheck][i] != Integer.MAX_VALUE) {
                    if (i == node) {
                        return true;
                    }
                    if (noVisits.contains(i)) {
                        noVisits.remove(i);
                        visits.add(i);
                    }
                }
            }
        }
        return false;
    }
}