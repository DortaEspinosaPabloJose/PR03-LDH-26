package top;

import java.util.ArrayList;
import java.util.Arrays;

import exposito.utilities.ExpositoUtilities;

/**
 * Datos de una instancia del problema de orientación por equipos con ventanas de tiempo (TOPTW).
 *
 * Modela el problema que resuelve el recomendador turístico: un conjunto de puntos de interés
 * (POIs), cada uno con una puntuación, una ventana de tiempo `[readyTime, dueTime]` y un tiempo
 * de servicio (visita); y un número limitado de rutas (vehículos/días) con una duración máxima.
 * El objetivo es construir las rutas que **maximicen la puntuación total** de los POIs visitados.
 *
 * ### Convenciones de indexación
 * - El nodo `0` es el depósito (punto de inicio y fin de todas las rutas).
 * - Los nodos `1..nodes` son los POIs.
 * - Los identificadores **mayores que `nodes`** son copias del depósito (una por ruta adicional).
 *   Todos los métodos de consulta (`getX()`, `getDistance()`, `getDueTime()`, ...) los
 *   traducen internamente al nodo `0`.
 * - Los vectores internos tienen tamaño `nodes + 1`.
 *
 * Las distancias son euclídeas y el tiempo de viaje coincide con la distancia (velocidad unitaria).
 *
 * @see TOPTWReader
 * @see TOPTWSolution
 */
public class TOPTW {
    /** Número de POIs (sin contar el depósito). */
    private int nodes;
    /** Coordenada X de cada nodo (índice 0 = depósito). */
    private double[] x;
    /** Coordenada Y de cada nodo (índice 0 = depósito). */
    private double[] y;
    /** Puntuación (interés) de cada nodo. */
    private double[] score;
    /** Instante de apertura de la ventana de tiempo de cada nodo. */
    private double[] readyTime;
    /** Instante de cierre de la ventana de tiempo de cada nodo. */
    private double[] dueTime;
    /** Tiempo de servicio (duración de la visita) de cada nodo. */
    private double[] serviceTime;
    /** Número de vehículos (rutas) disponibles. */
    private int vehicles;
    /** Contador de copias del depósito creadas durante la construcción de soluciones. */
    private int depots;
    /** Duración máxima de cada ruta (cierre de la ventana del depósito). */
    private double maxTimePerRoute;
    /** Número máximo de rutas permitidas. */
    private double maxRoutes;
    /** Matriz simétrica de distancias euclídeas entre nodos, de tamaño `(nodes + 1) x (nodes + 1)`. */
    private double[][] distanceMatrix;

    /**
     * Crea una instancia vacía con espacio para los nodos y rutas indicados.
     *
     * Reserva los vectores de datos y la matriz de distancias (inicializada a 0). Los datos de
     * cada nodo deben cargarse después con los métodos `setX()`, `setY()`, etc., y
     * a continuación llamar a `calculateDistanceMatrix()`.
     *
     * @param nodes  número de POIs (sin contar el depósito).
     * @param routes número de rutas (vehículos) disponibles.
     */
    public TOPTW(int nodes, int routes) {
        this.nodes = nodes;
        this.depots = 0;
        this.x = new double[this.nodes + 1];
        this.y = new double[this.nodes + 1];
        this.score = new double[this.nodes + 1];
        this.readyTime = new double[this.nodes + 1];
        this.dueTime = new double[this.nodes + 1];
        this.serviceTime = new double[this.nodes + 1];
        this.distanceMatrix = new double[this.nodes + 1][this.nodes + 1];
        for (int i = 0; i < this.nodes + 1; i++) {
            for (int j = 0; j < this.nodes + 1; j++) {
                this.distanceMatrix[i][j] = 0.0;
            }
        }
        this.maxRoutes = routes;
        this.vehicles = routes;
    }

    /**
     * Indica si un identificador corresponde a una copia del depósito.
     *
     * @param a identificador de nodo.
     * @return `true` si `a` es mayor que el número de POIs (es decir, es un depósito de ruta).
     */
    public boolean isDepot(int a) {
        if(a > this.nodes) {
            return true;
        }
        return false;
    }

    /**
     * Calcula la distancia total recorrida por una ruta dada como vector de nodos.
     *
     * @param route secuencia ordenada de nodos visitados.
     * @return suma de las distancias entre nodos consecutivos (0 si hay menos de 2 nodos).
     */
    public double getDistance(int[] route) {
        double distance = 0.0;
        for (int i = 0; i < route.length - 1; i++) {
            int node1 = route[i];
            int node2 = route[i + 1];
            distance += this.getDistance(node1, node2);
        }
        return distance;
    }

    /**
     * Calcula la distancia total recorrida por una ruta dada como lista de nodos.
     *
     * @param route secuencia ordenada de nodos visitados.
     * @return suma de las distancias entre nodos consecutivos (0 si hay menos de 2 nodos).
     */
    public double getDistance(ArrayList<Integer> route) {
        double distance = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            int node1 = route.get(i);
            int node2 = route.get(i + 1);
            distance += this.getDistance(node1, node2);
        }
        return distance;
    }

    /**
     * Calcula la distancia total de un conjunto de rutas.
     *
     * @param routes vector de rutas, cada una como lista ordenada de nodos.
     * @return suma de las distancias de todas las rutas.
     */
    public double getDistance(ArrayList<Integer>[] routes) {
        double distance = 0.0;
        for (ArrayList<Integer> route : routes) {
            distance += this.getDistance(route);
        }
        return distance;
    }


    /**
     * Rellena la matriz de distancias con la distancia euclídea entre cada par de nodos.
     *
     * Debe invocarse una vez cargadas todas las coordenadas. La matriz resultante es simétrica
     * y tiene ceros en la diagonal.
     */
    public void calculateDistanceMatrix() {
        for (int i = 0; i < this.nodes + 1; i++) {
            for (int j = 0; j < this.nodes + 1; j++) {
                if (i != j) {
                    double diffXs = this.x[i] - this.x[j];
                    double diffYs = this.y[i] - this.y[j];
                    this.distanceMatrix[i][j] = Math.sqrt(diffXs * diffXs + diffYs * diffYs);
                    this.distanceMatrix[j][i] = this.distanceMatrix[i][j];
                } else {
                    this.distanceMatrix[i][j] = 0.0;
                }
            }
        }
    }

    /**
     * Devuelve la duración máxima permitida para cada ruta.
     *
     * @return tiempo máximo por ruta.
     */
    public double getMaxTimePerRoute() {
        return maxTimePerRoute;
    }

    /**
     * Establece la duración máxima permitida para cada ruta.
     *
     * @param maxTimePerRoute tiempo máximo por ruta (normalmente el cierre de la ventana del depósito).
     */
    public void setMaxTimePerRoute(double maxTimePerRoute) {
        this.maxTimePerRoute = maxTimePerRoute;
    }

    /**
     * Devuelve el número máximo de rutas.
     *
     * @return número máximo de rutas.
     */
    public double getMaxRoutes() {
        return maxRoutes;
    }

    /**
     * Establece el número máximo de rutas.
     *
     * @param maxRoutes nuevo número máximo de rutas.
     */
    public void setMaxRoutes(double maxRoutes) {
        this.maxRoutes = maxRoutes;
    }

    /**
     * Devuelve el número de puntos de interés.
     *
     * @return número de POIs (sin contar el depósito); equivale a `getNodes()`.
     */
    public int getPOIs() {
        return this.nodes;
    }

    /**
     * Devuelve la distancia entre dos nodos.
     *
     * Los identificadores de copias del depósito se tratan como el nodo 0.
     *
     * @param i nodo origen.
     * @param j nodo destino.
     * @return distancia euclídea entre `i` y `j`.
     */
    public double getDistance(int i, int j) {
        if(this.isDepot(i)) { i=0; }
        if(this.isDepot(j)) { j=0; }
        return this.distanceMatrix[i][j];
    }

    /**
     * Devuelve el tiempo de viaje entre dos nodos.
     *
     * Con velocidad unitaria, el tiempo coincide con la distancia. Las copias del depósito se
     * tratan como el nodo 0.
     *
     * @param i nodo origen.
     * @param j nodo destino.
     * @return tiempo de desplazamiento de `i` a `j`.
     */
    public double getTime(int i, int j) {
        if(this.isDepot(i)) { i=0; }
        if(this.isDepot(j)) { j=0; }
        return this.distanceMatrix[i][j];
    }

    /**
     * Devuelve el número de nodos de la instancia.
     *
     * @return número de POIs (sin contar el depósito).
     */
    public int getNodes() {
        return this.nodes;
    }

    /**
     * Modifica el contador de nodos.
     *
     * @warning No redimensiona los vectores internos ni la matriz de distancias.
     *
     * @param nodes nuevo número de POIs.
     */
    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    /**
     * Devuelve la coordenada X de un nodo.
     *
     * @param index identificador del nodo (las copias del depósito equivalen al nodo 0).
     * @return coordenada X.
     */
    public double getX(int index) {
        if(this.isDepot(index)) { index=0; }
        return this.x[index];
    }

    /**
     * Establece la coordenada X de un nodo.
     *
     * @param index identificador del nodo.
     * @param x     nueva coordenada X.
     */
    public void setX(int index, double x) {
        this.x[index] = x;
    }

    /**
     * Devuelve la coordenada Y de un nodo.
     *
     * @param index identificador del nodo (las copias del depósito equivalen al nodo 0).
     * @return coordenada Y.
     */
    public double getY(int index) {
        if(this.isDepot(index)) { index=0; }
        return this.y[index];
    }

    /**
     * Establece la coordenada Y de un nodo.
     *
     * @param index identificador del nodo.
     * @param y     nueva coordenada Y.
     */
    public void setY(int index, double y) {
        this.y[index] = y;
    }

    /**
     * Devuelve la puntuación de un nodo.
     *
     * @param index identificador del nodo (las copias del depósito equivalen al nodo 0).
     * @return puntuación (interés) del nodo.
     */
    public double getScore(int index) {
        if(this.isDepot(index)) { index=0; }
        return this.score[index];
    }

    /**
     * Devuelve el vector completo de puntuaciones.
     *
     * @return vector de puntuaciones indexado por nodo (referencia directa, no una copia).
     */
    public double[] getScore() {
        return this.score;
    }

    /**
     * Establece la puntuación de un nodo.
     *
     * @param index identificador del nodo.
     * @param score nueva puntuación.
     */
    public void setScore(int index, double score) {
        this.score[index] = score;
    }

    /**
     * Devuelve el instante de apertura de la ventana de tiempo de un nodo.
     *
     * @param index identificador del nodo (las copias del depósito equivalen al nodo 0).
     * @return instante a partir del cual puede comenzar el servicio.
     */
    public double getReadyTime(int index) {
        if(this.isDepot(index)) { index=0; }
        return this.readyTime[index];
    }

    /**
     * Establece el instante de apertura de la ventana de tiempo de un nodo.
     *
     * @param index     identificador del nodo.
     * @param readyTime nuevo instante de apertura.
     */
    public void setReadyTime(int index, double readyTime) {
        this.readyTime[index] = readyTime;
    }

    /**
     * Devuelve el instante de cierre de la ventana de tiempo de un nodo.
     *
     * @param index identificador del nodo (las copias del depósito equivalen al nodo 0).
     * @return instante límite para llegar al nodo.
     */
    public double getDueTime(int index) {
        if(this.isDepot(index)) { index=0; }
        return this.dueTime[index];
    }

    /**
     * Establece el instante de cierre de la ventana de tiempo de un nodo.
     *
     * @param index   identificador del nodo.
     * @param dueTime nuevo instante de cierre.
     */
    public void setDueTime(int index, double dueTime) {
        this.dueTime[index] = dueTime;
    }

    /**
     * Devuelve el tiempo de servicio (duración de la visita) de un nodo.
     *
     * @param index identificador del nodo (las copias del depósito equivalen al nodo 0).
     * @return tiempo de servicio.
     */
    public double getServiceTime(int index) {
        if(this.isDepot(index)) { index=0; }
        return this.serviceTime[index];
    }

    /**
     * Establece el tiempo de servicio de un nodo.
     *
     * @param index       identificador del nodo.
     * @param serviceTime nuevo tiempo de servicio.
     */
    public void setServiceTime(int index, double serviceTime) {
        this.serviceTime[index] = serviceTime;
    }

    /**
     * Devuelve el número de vehículos (rutas) disponibles.
     *
     * @return número de vehículos.
     */
    public int getVehicles() {
        return this.vehicles;
    }

    /**
     * Genera una representación textual tabulada de la instancia.
     *
     * Incluye el número de nodos, una tabla con coordenadas, puntuación, ventana de tiempo y
     * tiempo de servicio de los nodos `0` a `nodes - 1`, y el número de vehículos.
     *
     * @return texto con los datos de la instancia.
     */
    @Override
    public String toString() {
        final int COLUMN_WIDTH = 15;
        String text = "Nodes: " + this.nodes + "\n";
        String[] strings = new String[]{"CUST NO.", "XCOORD.", "YCOORD.", "SCORE", "READY TIME", "DUE DATE", "SERVICE TIME"};
        int[] width = new int[strings.length];
        Arrays.fill(width, COLUMN_WIDTH);
        text += ExpositoUtilities.getFormat(strings, width) + "\n";
        for (int i = 0; i < this.nodes; i++) {
            strings = new String[strings.length];
            int index = 0;
            //strings[index++] = Integer.toString("" + i);
            strings[index++] = Integer.toString(i);
            strings[index++] = "" + this.x[i];
            strings[index++] = "" + this.y[i];
            strings[index++] = "" + this.score[i];
            strings[index++] = "" + this.readyTime[i];
            strings[index++] = "" + this.dueTime[i];
            strings[index++] = "" + this.serviceTime[i];
            text += ExpositoUtilities.getFormat(strings, width);
            text += "\n";
        }
        text += "Vehicles: " + this.vehicles + "\n";
        strings = new String[]{"VEHICLE", "CAPACITY"};
        width = new int[strings.length];
        Arrays.fill(width, COLUMN_WIDTH);
        text += ExpositoUtilities.getFormat(strings, width) + "\n";
        return text;
    }

    /**
     * Incrementa en uno el contador de nodos.
     *
     * @warning No amplía los vectores internos ni la matriz de distancias.
     *
     * @return nuevo número de nodos.
     */
    public int addNode() {
        this.nodes++;
        return this.nodes;
    }

    /**
     * Registra la creación de una nueva copia del depósito (una ruta más).
     *
     * @return número acumulado de copias del depósito creadas.
     */
    public int addNodeDepot() {
        this.depots++;
        return this.depots;
    }
}