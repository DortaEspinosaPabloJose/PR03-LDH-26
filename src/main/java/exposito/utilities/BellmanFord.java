package exposito.utilities;

import java.util.ArrayList;

/**
 * Algoritmo de Bellman-Ford para caminos mínimos desde el nodo 0 hasta el último nodo.
 *
 * Trabaja sobre un grafo dirigido descrito por una matriz de costes de enteros, donde
 * `Integer.MAX_VALUE` indica que no existe arco. Admite costes negativos, por lo que
 * también sirve para obtener **caminos máximos** (por ejemplo, la ruta crítica de un grafo
 * acíclico) cambiando el signo de los costes: el valor de `getValue()` es el opuesto de la
 * distancia mínima calculada.
 *
 * ### Uso
 * @code
 * ArrayList<Integer> path = new ArrayList<>();
 * BellmanFord bf = new BellmanFord(matrix, matrix.length, path);
 * bf.solve();
 * int value = bf.getValue();     // opuesto de la distancia mínima al último nodo
 * // path contiene el camino desde el último nodo hasta el nodo 0 (orden inverso)
 * @endcode
 *
 * @note No se detectan ciclos de coste negativo.
 */
public class BellmanFord {

    /** Valor que representa la distancia infinita (nodo aún no alcanzado). */
    private static final int INFINITY = 999999;
    /** Matriz de costes del grafo; `Integer.MAX_VALUE` indica ausencia de arco. */
    private final int[][] distanceMatrix;
    /** Nodos origen de cada arco existente (paralela a `edges2`). */
    private ArrayList<Integer> edges1 = null;
    /** Nodos destino de cada arco existente (paralela a `edges1`). */
    private ArrayList<Integer> edges2 = null;
    /** Número de nodos del grafo. */
    private final int nodes;
    /** Lista donde se deposita el camino resultante (del último nodo al nodo 0). */
    private final ArrayList<Integer> path;
    /** Distancia mínima desde el nodo 0 a cada nodo; `null` hasta ejecutar `solve()`. */
    private int[] distances = null;
    /** Resultado: opuesto de la distancia mínima al último nodo. */
    private int value;

    /**
     * Crea el resolutor y prepara la lista de arcos del grafo.
     *
     * @param distanceMatrix matriz de costes `nodes x nodes`; `Integer.MAX_VALUE` = sin arco.
     * @param nodes          número de nodos del grafo; el origen es el 0 y el destino `nodes - 1`.
     * @param path           lista (modificable) donde `solve()` almacenará el camino encontrado.
     */
    public BellmanFord(int[][] distanceMatrix, int nodes, ArrayList<Integer> path) {
        this.distanceMatrix = distanceMatrix;
        this.nodes = nodes;
        this.path = path;
        this.calculateEdges();
        this.value = BellmanFord.INFINITY;
    }

    /**
     * Construye las listas de arcos a partir de la matriz de costes.
     *
     * Se considera arco toda posición distinta de `Integer.MAX_VALUE`.
     */
    private void calculateEdges() {
        this.edges1 = new ArrayList<>();
        this.edges2 = new ArrayList<>();
        for (int i = 0; i < this.nodes; i++) {
            for (int j = 0; j < this.nodes; j++) {
                if (this.distanceMatrix[i][j] != Integer.MAX_VALUE) {
                    this.edges1.add(i);
                    this.edges2.add(j);
                }
            }
        }
    }

    /**
     * Devuelve las distancias calculadas desde el nodo 0.
     *
     * @return vector con la distancia mínima a cada nodo, o `null` si aún no se ha llamado a `solve()`.
     */
    public int[] getDistances() {
        return this.distances;
    }

    /**
     * Devuelve el valor del camino hallado.
     *
     * @return opuesto de la distancia mínima al último nodo tras `solve()`; antes de resolver,
     *         el valor inicial `999999`.
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Ejecuta el algoritmo de Bellman-Ford.
     *
     * Relaja todos los arcos `nodes - 1` veces a partir del nodo 0, reconstruye el camino hasta
     * el último nodo (añadiéndolo a la lista `path` en orden inverso, del último nodo al origen)
     * y almacena el resultado en `getValue()`.
     */
    public void solve() {
        int numEdges = this.edges1.size();
        int[] predecessor = new int[this.nodes];
        this.distances = new int[this.nodes];
        for (int i = 0; i < this.nodes; i++) {
            this.distances[i] = BellmanFord.INFINITY;
            predecessor[i] = -1;
        }
        this.distances[0] = 0;
        for (int i = 0; i < (this.nodes - 1); i++) {
            for (int j = 0; j < numEdges; j++) {
                int u = this.edges1.get(j);
                int v = this.edges2.get(j);
                if (this.distances[v] > this.distances[u] + this.distanceMatrix[u][v]) {
                    this.distances[v] = this.distances[u] + this.distanceMatrix[u][v];
                    predecessor[v] = u;
                }
            }
        }
        this.path.add(this.nodes - 1);
        int pred = predecessor[this.nodes - 1];
        while (pred != -1) {
            this.path.add(pred);
            pred = predecessor[pred];
        }
        this.value = -this.distances[this.nodes - 1];
    }
}