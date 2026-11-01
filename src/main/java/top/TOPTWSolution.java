package top;

import java.util.Arrays;

import exposito.utilities.ExpositoUtilities;

/**
 * Solución del TOPTW: conjunto de rutas que visitan POIs respetando sus ventanas de tiempo.
 *
 * ### Representación
 * Cada ruta es una **lista circular doblemente enlazada** que pasa por su depósito. Los enlaces
 * se guardan en dos vectores indexados por identificador de nodo:
 * `predecessors` y `successors`.
 *
 * - La ruta `0` usa el depósito `0`; las siguientes usan copias del depósito con
 *   identificadores `nodes + 1`, `nodes + 2`, ... (ver `addRoute()`).
 * - `routes[k]` contiene el identificador del depósito de la ruta `k`.
 * - El sucesor del último cliente de la ruta es su depósito, y el predecesor del depósito es
 *   el último cliente.
 *
 * Ejemplo de ruta con los clientes 5, 2 y 9 (depósito 0):
 * @code
 * 0 -> 5 -> 2 -> 9 -> 0
 * successors[0] = 5, successors[5] = 2, successors[2] = 9, successors[9] = 0
 * @endcode
 *
 * Una ruta vacía tiene al depósito como su propio predecesor y sucesor.
 *
 * @see TOPTW
 * @see TOPTWGRASP
 */
public class TOPTWSolution {
    /** Valor centinela para posiciones aún no inicializadas. */
    public static final int NO_INITIALIZED = -1;
    /** Instancia del problema a la que pertenece la solución. */
    private TOPTW problem;
    /** Predecesor de cada nodo en su ruta (indexado por identificador de nodo o depósito). */
    private int[] predecessors;
    /** Sucesor de cada nodo en su ruta (indexado por identificador de nodo o depósito). */
    private int[] successors;
    /** Tiempo de espera en cada POI hasta la apertura de su ventana de tiempo. */
    private double[] waitingTime;
    /** Posición de cada POI dentro de su ruta. */
    private int[] positionInRoute;

    /** Identificador del depósito de cada ruta (`routes[k]` = depósito de la ruta `k`). */
    private int[] routes;
    /** Número de vehículos que todavía no se han usado. */
    private int availableVehicles;
    /** Valor de la función objetivo (o `TOPTWEvaluator.NO_EVALUATED` si no se ha evaluado). */
    private double objectiveFunctionValue;

    /**
     * Crea una solución vacía para un problema.
     *
     * Reserva las estructuras de enlaces, tiempos de espera y posiciones, inicializadas con
     * `NO_INITIALIZED`. Antes de construir rutas debe invocarse `initSolution()`.
     *
     * @param problem instancia del TOPTW que se resolverá.
     */
    public TOPTWSolution(TOPTW problem) {
        this.problem = problem;
        this.availableVehicles = this.problem.getVehicles();
        this.predecessors = new int[this.problem.getPOIs()+this.problem.getVehicles()];
        this.successors = new int[this.problem.getPOIs()+this.problem.getVehicles()];
        this.waitingTime = new double[this.problem.getPOIs()];
        this.positionInRoute = new int[this.problem.getPOIs()];
        Arrays.fill(this.predecessors, TOPTWSolution.NO_INITIALIZED);
        Arrays.fill(this.successors, TOPTWSolution.NO_INITIALIZED);
        Arrays.fill(this.waitingTime, TOPTWSolution.NO_INITIALIZED);
        Arrays.fill(this.positionInRoute, TOPTWSolution.NO_INITIALIZED);
        this.routes = new int[this.problem.getVehicles()];
        this.objectiveFunctionValue = TOPTWEvaluator.NO_EVALUATED;
    }

    /**
     * Reinicia la solución dejando una única ruta vacía (la ruta 0 con el depósito 0).
     *
     * Descarta los enlaces y rutas anteriores y deja `vehículos - 1` vehículos disponibles.
     */
    public void initSolution() {
        this.predecessors = new int[this.problem.getPOIs()+this.problem.getVehicles()];
        this.successors = new int[this.problem.getPOIs()+this.problem.getVehicles()];
        Arrays.fill(this.predecessors, TOPTWSolution.NO_INITIALIZED);
        Arrays.fill(this.successors, TOPTWSolution.NO_INITIALIZED);
        this.routes = new int[this.problem.getVehicles()];
        Arrays.fill(this.routes, TOPTWSolution.NO_INITIALIZED);
        this.routes[0] = 0;
        this.predecessors[0] = 0;
        this.successors[0] = 0;
        this.availableVehicles = this.problem.getVehicles() - 1;
    }

    /**
     * Indica si un nodo es el depósito de alguna de las rutas de la solución.
     *
     * @param c identificador de nodo.
     * @return `true` si `c` figura en la lista de depósitos de las rutas.
     */
    public boolean isDepot(int c) {
        for(int i = 0; i < this.routes.length; i++) {
            if(c==this.routes[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compara dos soluciones por sus enlaces de predecesores.
     *
     * @note Sólo se comparan los vectores de predecesores; no se tienen en cuenta el resto de
     *       campos. La clase no redefine `hashCode()`.
     *
     * @param o objeto con el que comparar.
     * @return `true` si es la misma instancia o una `TOPTWSolution` con idénticos predecesores.
     */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        TOPTWSolution otherSolution = (TOPTWSolution) o;

        for (int i = 0; i < this.predecessors.length; i++) {
            if (this.predecessors[i] != otherSolution.predecessors[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Devuelve el número de vehículos todavía sin usar.
     *
     * @return vehículos disponibles.
     */
    public int getAvailableVehicles() {
        return this.availableVehicles;
    }

    /**
     * Devuelve el número de rutas creadas hasta el momento.
     *
     * @return vehículos totales menos vehículos disponibles.
     */
    public int getCreatedRoutes() {
        return this.problem.getVehicles() - this.availableVehicles;
    }

    /**
     * Devuelve la distancia entre dos nodos según el problema.
     *
     * @param x nodo origen.
     * @param y nodo destino.
     * @return distancia euclídea entre ambos.
     */
    public double getDistance(int x, int y) {
        return this.problem.getDistance(x, y);
    }

    /**
     * Establece el número de vehículos disponibles.
     *
     * @param availableVehicles nuevo número de vehículos sin usar.
     */
    public void setAvailableVehicles(int availableVehicles) {
        this.availableVehicles = availableVehicles;
    }

    /**
     * Devuelve el predecesor de un nodo.
     *
     * @param customer identificador del nodo.
     * @return predecesor en su ruta, o `NO_INITIALIZED` si el nodo no está en ninguna ruta.
     */
    public int getPredecessor(int customer) {
        return this.predecessors[customer];
    }

    /**
     * Devuelve el vector completo de predecesores.
     *
     * @return vector de predecesores (referencia directa, no una copia).
     */
    public int[] getPredecessors() {
        return this.predecessors;
    }

    /**
     * Devuelve la instancia del problema.
     *
     * @return problema TOPTW asociado.
     */
    public TOPTW getProblem() {
        return this.problem;
    }

    /**
     * Devuelve el valor almacenado de la función objetivo.
     *
     * @return valor objetivo, o `TOPTWEvaluator.NO_EVALUATED` si no se ha evaluado.
     */
    public double getObjectiveFunctionValue() {
        return this.objectiveFunctionValue;
    }

    /**
     * Devuelve la posición de un POI dentro de su ruta.
     *
     * @param customer identificador del POI.
     * @return posición, o `NO_INITIALIZED` si no se ha establecido.
     */
    public int getPositionInRoute(int customer) {
        return this.positionInRoute[customer];
    }

    /**
     * Devuelve el sucesor de un nodo.
     *
     * @param customer identificador del nodo.
     * @return sucesor en su ruta, o `NO_INITIALIZED` si el nodo no está en ninguna ruta.
     */
    public int getSuccessor(int customer) {
        return this.successors[customer];
    }

    /**
     * Devuelve el vector completo de sucesores.
     *
     * @return vector de sucesores (referencia directa, no una copia).
     */
    public int[] getSuccessors() {
        return this.successors;
    }

    /**
     * Devuelve el depósito de una ruta.
     *
     * @param index índice de la ruta, entre 0 y `getCreatedRoutes() - 1`.
     * @return identificador del nodo depósito de esa ruta.
     */
    public int getIndexRoute(int index) {
        return this.routes[index];
    }

    /**
     * Devuelve el tiempo de espera de un POI.
     *
     * @param customer identificador del POI.
     * @return tiempo de espera hasta la apertura de su ventana de tiempo.
     */
    public double getWaitingTime(int customer) {
        return this.waitingTime[customer];
    }

    /**
     * Almacena el valor de la función objetivo.
     *
     * @param objectiveFunctionValue valor objetivo calculado.
     */
    public void setObjectiveFunctionValue(double objectiveFunctionValue) {
        this.objectiveFunctionValue = objectiveFunctionValue;
    }

    /**
     * Establece la posición de un POI dentro de su ruta.
     *
     * @param customer identificador del POI.
     * @param position nueva posición.
     */
    public void setPositionInRoute(int customer, int position) {
        this.positionInRoute[customer] = position;
    }

    /**
     * Establece el predecesor de un nodo.
     *
     * @param customer    identificador del nodo.
     * @param predecessor identificador del nuevo predecesor.
     */
    public void setPredecessor(int customer, int predecessor) {
        this.predecessors[customer] = predecessor;
    }

    /**
     * Establece el sucesor de un nodo.
     *
     * @param customer identificador del nodo.
     * @param succesor identificador del nuevo sucesor.
     */
    public void setSuccessor(int customer, int succesor) {
        this.successors[customer] = succesor;
    }

    /**
     * Establece el tiempo de espera de un POI.
     *
     * @param customer    identificador del POI.
     * @param waitingTime nuevo tiempo de espera.
     */
    public void setWaitingTime(int customer, int waitingTime) {
        this.waitingTime[customer] = waitingTime;
    }

    /**
     * Genera un informe textual detallado de la solución.
     *
     * Recorre cada ruta calculando los instantes de llegada y salida, y comprueba la
     * factibilidad (ventanas de tiempo y duración máxima por ruta). El texto contiene:
     * - un resumen: rutas en formato `0 - 5 - 2 - 0`, si la solución es factible, la puntuación
     *   total y el coste de tiempo total;
     * - los datos del problema y, por cada ruta, una tabla con coordenadas, ventana de tiempo,
     *   instantes de llegada y salida y tiempo de servicio de cada parada.
     *
     * @return informe de la solución listo para imprimir.
     */
    public String getInfoSolution() {
        final int COLUMN_WIDTH = 15;
        String text = "\n"+"NODES: " + this.problem.getPOIs() + "\n" + "MAX TIME PER ROUTE: " + this.problem.getMaxTimePerRoute() + "\n" + "MAX NUMBER OF ROUTES: " + this.problem.getMaxRoutes() + "\n";
        String textSolution = "\n"+"SOLUTION: "+"\n";
        double costTimeSolution = 0.0, fitnessScore = 0.0;
        boolean validSolution = true;
        for(int k = 0; k < this.getCreatedRoutes(); k++) { // rutas creadas
            String[] strings = new String[]{"\n" + "ROUTE " + k };
            int[] width = new int[strings.length];
            Arrays.fill(width, COLUMN_WIDTH);
            text += ExpositoUtilities.getFormat(strings, width) + "\n";
            strings = new String[]{"CUST NO.", "X COORD.", "Y. COORD.", "READY TIME", "DUE DATE", "ARRIVE TIME", " LEAVE TIME", "SERVICE TIME"};
            width = new int[strings.length];
            Arrays.fill(width, COLUMN_WIDTH);
            text += ExpositoUtilities.getFormat(strings, width) + "\n";
            strings = new String[strings.length];
            int depot = this.getIndexRoute(k);
            int pre=-1, suc=-1;
            double costTimeRoute = 0.0, fitnessScoreRoute = 0.0;
            pre = depot;
            int index = 0;
            strings[index++] = "" + pre;
            strings[index++] = "" + this.getProblem().getX(pre);
            strings[index++] = "" + this.getProblem().getY(pre);
            strings[index++] = "" + this.getProblem().getReadyTime(pre);
            strings[index++] = "" + this.getProblem().getDueTime(pre);
            strings[index++] = "" + 0;
            strings[index++] = "" + 0;
            strings[index++] = "" + this.getProblem().getServiceTime(pre);
            text += ExpositoUtilities.getFormat(strings, width);
            text += "\n";
            do {                // recorremos la ruta
                index = 0;
                suc = this.getSuccessor(pre);
                textSolution += pre+" - ";
                strings[index++] = "" + suc;
                strings[index++] = "" + this.getProblem().getX(suc);
                strings[index++] = "" + this.getProblem().getY(suc);
                strings[index++] = "" + this.getProblem().getReadyTime(suc);
                strings[index++] = "" + this.getProblem().getDueTime(suc);
                costTimeRoute += this.getDistance(pre, suc);
                if(costTimeRoute < (this.getProblem().getDueTime(suc))) {
                    if(costTimeRoute < this.getProblem().getReadyTime(suc)) {
                        costTimeRoute = this.getProblem().getReadyTime(suc);
                    }
                    strings[index++] = "" + costTimeRoute;
                    costTimeRoute +=  this.getProblem().getServiceTime(suc);
                    strings[index++] = "" + costTimeRoute;
                    strings[index++] = "" + this.getProblem().getServiceTime(pre);
                    if(costTimeRoute > this.getProblem().getMaxTimePerRoute()) { validSolution = false; }
                    fitnessScoreRoute += this.problem.getScore(suc);
                } else { validSolution = false; }
                pre = suc;
                text += ExpositoUtilities.getFormat(strings, width);
                text += "\n";
            } while(suc != depot);
            textSolution += suc+"\n";
            costTimeSolution += costTimeRoute;
            fitnessScore += fitnessScoreRoute;
        }
        textSolution += "FEASIBLE SOLUTION: "+validSolution+"\n"+"SCORE: "+fitnessScore+"\n"+"TIME COST: "+costTimeSolution+"\n";
        return textSolution+text;
    }

    /**
     * Calcula la función objetivo: la puntuación total de los POIs visitados.
     *
     * Recorre todas las rutas creadas sumando la puntuación de cada nodo visitado. El resultado
     * **no** se almacena en la solución (ver `setObjectiveFunctionValue()`).
     *
     * @return suma de las puntuaciones de todos los POIs incluidos en alguna ruta.
     */
    public double evaluateFitness() {
        double objectiveFunction = 0.0;
        double objectiveFunctionPerRoute = 0.0;
        for(int k = 0; k < this.getCreatedRoutes(); k++) {
            int depot = this.getIndexRoute(k);
            int pre=depot, suc = -1;
            do {
                suc = this.getSuccessor(pre);
                objectiveFunctionPerRoute = objectiveFunctionPerRoute + this.problem.getScore(suc);
                pre = suc;
            } while((suc != depot));
            objectiveFunction = objectiveFunction + objectiveFunctionPerRoute;
            objectiveFunctionPerRoute = 0.0;
        }
        return objectiveFunction;
    }

    /**
     * Crea una nueva ruta vacía con una copia del depósito.
     *
     * El identificador del nuevo depósito es el del último depósito creado más uno (el primero
     * es `nodes + 1`). La ruta se registra en `routes`, se decrementa el número de vehículos
     * disponibles, se enlaza el depósito consigo mismo y se informa al problema
     * (`TOPTW.addNodeDepot()`).
     *
     * @return identificador del nuevo nodo depósito.
     */
    public int addRoute() {
        int depot = this.problem.getPOIs();
        depot++;
        int routePos = 1;
        for(int i = 0; i < this.routes.length; i++) {
            if(this.routes[i] != -1 && this.routes[i] != 0) {
                depot = this.routes[i];
                depot++;
                routePos = i+1;
            }
        }
        this.routes[routePos] = depot;
        this.availableVehicles--;
        this.predecessors[depot] = depot;
        this.successors[depot] = depot;
        this.problem.addNodeDepot();
        return depot;
    }

    /**
     * Imprime por consola las rutas de la solución y su puntuación.
     *
     * Cada ruta se muestra como `0 - 5 - 2 - 0`, seguida de la línea `SC=<puntuación>`.
     *
     * @return puntuación total de la solución (`evaluateFitness()`).
     */
    public double printSolution() {
        for(int k = 0; k < this.getCreatedRoutes(); k++) {
            int depot = this.getIndexRoute(k);
            int pre=depot, suc = -1;
            do {
                suc = this.getSuccessor(pre);
                System.out.print(pre+" - ");
                pre = suc;
            } while((suc != depot));
            System.out.println(suc+"  ");
        }
        double fitness = this.evaluateFitness();
        System.out.println("SC="+fitness);
        return fitness;
    }

}