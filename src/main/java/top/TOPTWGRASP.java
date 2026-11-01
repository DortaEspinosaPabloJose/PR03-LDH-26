package top;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * Fase constructiva de la metaheurística GRASP para el TOPTW.
 *
 * Implementa un procedimiento GRASP (*Greedy Randomized Adaptive Search Procedure*)
 * para el problema de orientación por equipos con ventanas de tiempo
 * (*Team Orienteering Problem with Time Windows*, TOPTW), núcleo del recomendador
 * turístico RTVRPTW. Cada ruta representa el itinerario de un día/vehículo, cada cliente
 * un punto de interés (POI) y el objetivo es **maximizar la puntuación total** de los
 * POIs visitados respetando sus ventanas de tiempo y la duración máxima de cada ruta.
 *
 * ### Esquema del algoritmo
 * 1. Se construye una solución voraz aleatorizada (`computeGreedySolution()`):
 *    - se evalúa la mejor posición de inserción de cada cliente libre
 *      (`comprehensiveEvaluation()`);
 *    - se ordenan los candidatos por coste incremental y se forma la lista restringida de
 *      candidatos (RCL) con los `maxSizeRCL` mejores;
 *    - se elige un elemento de la RCL (aleatoria o difusa) y se inserta en la ruta
 *      (`updateSolution()`);
 *    - si no quedan candidatos factibles se abre una nueva ruta, mientras haya vehículos.
 * 2. Se evalúa la solución y se actualizan las estadísticas (media y mejor valor).
 * 3. (Pendiente) Búsqueda local sobre la solución construida.
 *
 * ### Formato de un candidato
 * Los candidatos se representan como `double[5]`:
 *
 * | Índice | Contenido                                                   |
 * |:------:|-------------------------------------------------------------|
 * | 0      | Identificador del cliente (POI) a insertar                  |
 * | 1      | Índice de la ruta en la que se inserta                      |
 * | 2      | Nodo predecesor tras el cual se inserta el cliente          |
 * | 3      | Coste: instante en que termina la ruta tras la inserción    |
 * | 4      | Puntuación (score) del cliente                              |
 *
 * @see TOPTWSolution
 * @see TOPTW
 */
public class TOPTWGRASP {
    /** Valor centinela para indicar que una solución todavía no ha sido evaluada. */
    public static double NO_EVALUATED = -1.0;

    /** Solución sobre la que trabaja el algoritmo; se reinicia en cada construcción. */
    private TOPTWSolution solution;
    /** Tiempo de cómputo asociado a la solución (reservado; actualmente siempre 0). */
    private int solutionTime;
    /** Generador de números aleatorios usado en las selecciones de la RCL. */
    private Random randomFactor = new Random();

    /**
     * Crea un GRASP asociado a una solución (y, por tanto, a un problema TOPTW).
     *
     * @param sol solución inicial que contiene la instancia del problema; será reutilizada
     *            y sobrescrita en cada iteración de `GRASP()`.
     */
    public TOPTWGRASP(TOPTWSolution sol){
        this.solution = sol;
        this.solutionTime = 0;
    }

    /*procedure GRASP(Max Iterations,Seed)
        1 Read Input();
        2 for k = 1, . . . , Max Iterations do
            3 Solution ← Greedy Randomized Construction(Seed);
            4 Solution ← Local Search(Solution);
            5 Update Solution(Solution,Best Solution);
        6 end;
        7 return Best Solution;
    end GRASP*/

    /*procedure Greedy Randomized Construction(Seed)
        Solution ← ∅;
        Evaluate the incremental costs of the candidate elements;
        while Solution is not a complete solution do
            Build the restricted candidate list (RCL);
            Select an element s from the RCL at random;
            Solution ← Solution ∪ {s};
            Reevaluate the incremental costs;
        end;
        return Solution;
    end Greedy Randomized Construction.*/

    /**
     * Ejecuta el procedimiento GRASP durante un número fijo de iteraciones.
     *
     * En cada iteración construye una solución con `computeGreedySolution()`, la evalúa con
     * `TOPTWSolution.evaluateFitness()` y acumula la media y el mejor valor de la función
     * objetivo (suma de scores). El bloque de **búsqueda local** está aún por implementar.
     *
     * **Efectos por consola:** imprime el informe detallado de cada solución
     * (`TOPTWSolution.getInfoSolution()`), y al final la media y el mejor valor obtenidos.
     * No se conserva la mejor solución, sólo su valor.
     *
     * @param maxIterations número de construcciones (iteraciones) a realizar; debe ser mayor que 0.
     * @param maxSizeRCL    tamaño máximo de la lista restringida de candidatos (RCL).
     *                      Valores pequeños son más voraces; valores grandes, más aleatorios.
     */
    public void GRASP(int maxIterations, int maxSizeRCL) {
        double averageFitness = 0.0;
        double bestSolution = 0.0;
        for(int i = 0; i < maxIterations; i++) {

            this.computeGreedySolution(maxSizeRCL);

            // IMPRIMIR SOLUCION
            double fitness = this.solution.evaluateFitness();
            System.out.println(this.solution.getInfoSolution());
            //System.out.println("Press Any Key To Continue...");
            //new java.util.Scanner(System.in).nextLine();
            averageFitness += fitness;
            if(bestSolution < fitness) {
                bestSolution = fitness;
            }
            //double fitness = this.solution.printSolution();

            /******
             *
             * BÚSQUEDA LOCAL
             *
             */
        }
        averageFitness = averageFitness/maxIterations;
        System.out.println(" --> MEDIA: "+averageFitness);
        System.out.println(" --> MEJOR SOLUCION: "+bestSolution);
    }

    /**
     * Selecciona al azar (distribución uniforme) una posición de la RCL.
     *
     * @param maxTRCL número de elementos de la RCL (límite superior exclusivo).
     * @return posición elegida, en el intervalo `[0, maxTRCL)`.
     * @throws IllegalArgumentException si `maxTRCL` es menor o igual que 0.
     */
    public int aleatorySelectionRCL(int maxTRCL) {
        int low = 0;
        int high = maxTRCL;
        int posSelected = this.randomFactor.nextInt(high-low) + low;
        return posSelected;
    }

    /**
     * Selección difusa determinista: elige el candidato con mejor grado de pertenencia.
     *
     * El grado de pertenencia de un candidato es `1 - score / maxScore`, donde `maxScore` es la
     * mayor puntuación de todos los POIs (`getMaxScore()`). Cuanto menor es el valor, mejor es
     * el candidato; por tanto se elige el de mayor puntuación. En caso de empate gana el primero.
     *
     * @param rcl lista restringida de candidatos (cada uno con el formato `double[5]`
     *            descrito en la clase; se usa la posición 4, el score).
     * @return posición dentro de `rcl` del candidato seleccionado, o -1 si `rcl` está vacía.
     */
    public int fuzzySelectionBestFDRCL(ArrayList< double[] > rcl) {
        double[] membershipFunction = new double[rcl.size()];
        double maxSc = this.getMaxScore();
        for(int j=0; j < rcl.size(); j++) {
            membershipFunction[j] = 1 - ((rcl.get(j)[4])/maxSc);
        }
        double minMemFunc = Double.MAX_VALUE;
        int posSelected = -1;
        for(int i = 0; i < rcl.size(); i++) {
            if(minMemFunc > membershipFunction[i]) {
                minMemFunc = membershipFunction[i];
                posSelected = i;
            }
        }
        return posSelected;
    }

    /**
     * Selección difusa con corte alfa y elección aleatoria.
     *
     * Se calcula el grado de pertenencia `1 - score / maxScore` de cada candidato y se conservan
     * los que cumplen `pertenencia <= alpha` (corte alfa). Entre ellos se elige uno al azar.
     * Si ninguno supera el corte, se elige al azar entre todos los elementos de la RCL.
     *
     * @param rcl   lista restringida de candidatos (formato `double[5]`; se usa el score en la posición 4).
     * @param alpha umbral del corte alfa, en `[0, 1]`. Valores bajos exigen candidatos con mayor score.
     * @return posición dentro de `rcl` del candidato seleccionado.
     */
    public int fuzzySelectionAlphaCutRCL(ArrayList< double[] > rcl, double alpha) {
        ArrayList< double[] > rclAlphaCut = new ArrayList< double[] >();
        ArrayList< Integer > rclPos = new ArrayList< Integer >();
        double[] membershipFunction = new double[rcl.size()];
        double maxSc = this.getMaxScore();
        for(int j=0; j < rcl.size(); j++) {
            membershipFunction[j] = 1 - ((rcl.get(j)[4])/maxSc);
            if(membershipFunction[j] <= alpha) {
                rclAlphaCut.add(rcl.get(j));
                rclPos.add(j);
            }
        }
        int posSelected = -1;
        if(rclAlphaCut.size() > 0) {
            posSelected = rclPos.get(aleatorySelectionRCL(rclAlphaCut.size()));
        } else {
            posSelected = aleatorySelectionRCL(rcl.size());
        }
        return posSelected;
    }

    /**
     * Construye una solución voraz aleatorizada completa.
     *
     * Reinicia la solución (`TOPTWSolution.initSolution()`) y, mientras queden clientes sin
     * asignar y sea posible, repite:
     * 1. Reevaluar la mejor inserción de cada cliente libre (`comprehensiveEvaluation()`) y
     *    ordenar los candidatos por coste ascendente.
     * 2. Construir la RCL con los `maxSizeRCL` mejores candidatos.
     * 3. Elegir uno de la RCL y insertarlo (`updateSolution()`).
     * 4. Si no hay ningún candidato factible, abrir una nueva ruta (`TOPTWSolution.addRoute()`);
     *    si ya se usaron todos los vehículos, terminar.
     *
     * El método de selección está fijado internamente (`selection = 3`, corte alfa con
     * `alpha = 0.8`); las opciones son: 1 = aleatoria, 2 = difusa determinista,
     * 3 = difusa con corte alfa.
     *
     * Se mantiene una lista de tiempos de salida por ruta: `departureTimesPerClient.get(k).get(n)`
     * es el instante en que se abandona el nodo `n` en la ruta `k` (0 para el depósito).
     *
     * @param maxSizeRCL tamaño máximo de la RCL; se limita al número de candidatos disponibles.
     */
    public void computeGreedySolution(int maxSizeRCL) {
        // inicialización
        this.solution.initSolution();

        // tiempo de salida y score por ruta y cliente
        ArrayList<ArrayList<Double>> departureTimesPerClient = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> init = new ArrayList<Double>();
        for(int z = 0; z < this.solution.getProblem().getPOIs()+this.solution.getProblem().getVehicles(); z++) {init.add(0.0);}
        departureTimesPerClient.add(0, init);

        // clientes
        ArrayList<Integer> customers = new ArrayList<Integer>();
        for(int j = 1; j <= this.solution.getProblem().getPOIs(); j++) { customers.add(j); }

        // Evaluar coste incremental de los elementos candidatos
        ArrayList< double[] > candidates = this.comprehensiveEvaluation(customers, departureTimesPerClient);

        Collections.sort(candidates, new Comparator<double[]>() {
            public int compare(double[] a, double[] b) {
                return Double.compare(a[a.length-2], b[b.length-2]);
            }
        });

        int maxTRCL = maxSizeRCL;
        boolean existCandidates = true;

        while(!customers.isEmpty() && existCandidates) {
            if(!candidates.isEmpty()) {
                //Construir lista restringida de candidatos
                ArrayList< double[] > rcl = new ArrayList< double[] >();
                maxTRCL = maxSizeRCL;
                if(maxTRCL > candidates.size()) { maxTRCL = candidates.size(); }
                for(int j=0; j < maxTRCL; j++) { rcl.add(candidates.get(j)); }

                //Selección aleatoria o fuzzy de candidato de la lista restringida
                int posSelected = -1;
                int selection = 3;
                double alpha = 0.8;
                switch (selection) {
                    case 1:  posSelected = this.aleatorySelectionRCL(maxTRCL);  // Selección aleatoria
                        break;
                    case 2:  posSelected = this.fuzzySelectionBestFDRCL(rcl);   // Selección fuzzy con mejor valor de alpha
                        break;
                    case 3:  posSelected = this.fuzzySelectionAlphaCutRCL(rcl, alpha); // Selección fuzzy con alpha corte aleatoria
                        break;
                    default: posSelected = this.aleatorySelectionRCL(maxTRCL);  // Selección aleatoria por defecto
                        break;
                }

                double[] candidateSelected = rcl.get(posSelected);
                for(int j=0; j < customers.size(); j++) {
                    if(customers.get(j)==candidateSelected[0]) {
                        customers.remove(j);
                    }
                }

                updateSolution(candidateSelected, departureTimesPerClient);

            } else { // No hay candidatos a insertar en la solución, crear otra ruta
                if(this.solution.getCreatedRoutes() < this.solution.getProblem().getVehicles()) {
                    int newDepot = this.solution.addRoute();
                    ArrayList<Double> initNew = new ArrayList<Double>();
                    for(int z = 0; z < this.solution.getProblem().getPOIs()+this.solution.getProblem().getVehicles(); z++) {initNew.add(0.0);}
                    departureTimesPerClient.add(initNew);
                }
                else {
                    existCandidates = false;
                }
            }
            //Reevaluar coste incremental de los elementos candidatos
            candidates.clear();
            candidates = this.comprehensiveEvaluation(customers, departureTimesPerClient);
            Collections.sort(candidates, new Comparator<double[]>() {
                public int compare(double[] a, double[] b) {
                    return Double.compare(a[a.length-2], b[b.length-2]);
                }
            });
        }

    }

    /**
     * Inserta en la solución el candidato elegido y recalcula los tiempos de salida de su ruta.
     *
     * El cliente se enlaza justo después de su predecesor en la lista circular de la ruta y se
     * actualizan los tiempos de salida de todos los nodos posteriores, aplicando la espera hasta
     * la apertura de la ventana de tiempo y sumando el tiempo de servicio.
     *
     * @param candidateSelected candidato con formato `[cliente, ruta, predecesor, coste, score]`.
     * @param departureTimes    tiempos de salida por ruta y nodo; se modifica la lista de la
     *                          ruta afectada.
     */
    public void updateSolution(double[] candidateSelected, ArrayList< ArrayList< Double > > departureTimes) {
        // Inserción del cliente en la ruta  return: cliente, ruta, predecesor, coste
        this.solution.setPredecessor((int)candidateSelected[0], (int)candidateSelected[2]);
        this.solution.setSuccessor((int)candidateSelected[0], this.solution.getSuccessor((int)candidateSelected[2]));
        this.solution.setSuccessor((int)candidateSelected[2], (int)candidateSelected[0]);
        this.solution.setPredecessor(this.solution.getSuccessor((int)candidateSelected[0]), (int)candidateSelected[0]);

        // Actualización de las estructuras de datos y conteo a partir de la posición a insertar
        double costInsertionPre = departureTimes.get((int)candidateSelected[1]).get((int)candidateSelected[2]);
        ArrayList<Double> route = departureTimes.get((int)candidateSelected[1]);
        int pre=(int)candidateSelected[2], suc=-1;
        int depot = this.solution.getIndexRoute((int)candidateSelected[1]);
        do {
            suc = this.solution.getSuccessor(pre);
            costInsertionPre += this.solution.getDistance(pre, suc);

            if(costInsertionPre < this.solution.getProblem().getReadyTime(suc)) {
                costInsertionPre = this.solution.getProblem().getReadyTime(suc);
            }
            costInsertionPre += this.solution.getProblem().getServiceTime(suc);

            if(!this.solution.isDepot(suc))
                route.set(suc, costInsertionPre);
            pre = suc;
        } while((suc != depot));

        // Actualiza tiempos
        departureTimes.set((int)candidateSelected[1], route);
    }

    /**
     * Evalúa, para cada cliente libre, su mejor posición de inserción factible.
     *
     * Para cada cliente se prueban todas las posiciones de todas las rutas creadas (tras cada
     * nodo `pre`, incluido el depósito). Una inserción es **factible** si:
     * - se llega al cliente antes del cierre de su ventana de tiempo (se espera si se llega antes
     *   de la apertura);
     * - la salida del cliente permite llegar a tiempo a su sucesor y a todos los nodos
     *   posteriores de la ruta (propagación del retraso);
     * - ningún instante supera la duración máxima de la ruta (`TOPTW.getMaxTimePerRoute()`).
     *
     * De entre las posiciones factibles se conserva la de menor coste, entendido como el
     * instante en que finaliza la ruta tras la inserción. Los clientes sin ninguna inserción
     * factible no aparecen en el resultado.
     *
     * @param customers      clientes (POIs) aún no asignados a ninguna ruta.
     * @param departureTimes tiempos de salida por ruta y nodo (ver `computeGreedySolution()`).
     * @return lista de candidatos, uno por cliente, con formato
     *         `[cliente, ruta, predecesor, coste de tiempo, score]`.
     */
    public ArrayList< double[] > comprehensiveEvaluation(ArrayList<Integer> customers, ArrayList< ArrayList< Double > > departureTimes) {
        ArrayList< double[] > candidatesList = new ArrayList< double[] >();
        double[] infoCandidate = new double[5];
        boolean validFinalInsertion = true;
        infoCandidate[0] = -1;
        infoCandidate[1] = -1;
        infoCandidate[2] = -1;
        infoCandidate[3] = Double.MAX_VALUE;
        infoCandidate[4] = -1;

        for(int c = 0; c < customers.size(); c++) { // clientes disponibles
            for(int k = 0; k < this.solution.getCreatedRoutes(); k++) { // rutas creadas
                validFinalInsertion = true;
                int depot = this.solution.getIndexRoute(k);
                int pre=-1, suc=-1;
                double costInsertion = 0;
                pre = depot;
                int candidate = customers.get(c);
                do {                                                // recorremos la ruta
                    validFinalInsertion = true;
                    suc = this.solution.getSuccessor(pre);
                    // Llegada al candidato si se inserta tras "pre"; debe ser anterior al cierre de su ventana
                    double timesUntilPre = departureTimes.get(k).get(pre) + this.solution.getDistance(pre, candidate);
                    if(timesUntilPre < (this.solution.getProblem().getDueTime(candidate))) {
                        double costCand = 0;
                        if(timesUntilPre < this.solution.getProblem().getReadyTime(candidate)) {
                            costCand = this.solution.getProblem().getReadyTime(candidate);
                        } else { costCand = timesUntilPre; }
                        costCand +=  this.solution.getProblem().getServiceTime(candidate);
                        if(costCand > this.solution.getProblem().getMaxTimePerRoute()) { validFinalInsertion = false; }

                        // Comprobar TW desde candidate hasta sucesor
                        double timesUntilSuc = costCand + this.solution.getDistance(candidate, suc);
                        if(timesUntilSuc < (this.solution.getProblem().getDueTime(suc))) {
                            double costSuc = 0;
                            if(timesUntilSuc < this.solution.getProblem().getReadyTime(suc)) {
                                costSuc = this.solution.getProblem().getReadyTime(suc);
                            } else { costSuc = timesUntilSuc; }
                            costSuc +=  this.solution.getProblem().getServiceTime(suc);
                            costInsertion = costSuc;
                            if(costSuc > this.solution.getProblem().getMaxTimePerRoute()) { validFinalInsertion = false;}

                            // Propagar el retraso a los nodos posteriores hasta el depósito de la ruta
                            int pre2=suc, suc2 = -1;
                            if(suc != depot)
                                do {
                                    suc2 = this.solution.getSuccessor(pre2);
                                    double timesUntilSuc2 = costInsertion + this.solution.getDistance(pre2, suc2);
                                    if(timesUntilSuc2 < (this.solution.getProblem().getDueTime(suc2))) {
                                        if(timesUntilSuc2 < this.solution.getProblem().getReadyTime(suc2)) {
                                            costInsertion = this.solution.getProblem().getReadyTime(suc2);
                                        } else { costInsertion = timesUntilSuc2; }
                                        costInsertion += this.solution.getProblem().getServiceTime(suc2);
                                        if(costInsertion > this.solution.getProblem().getMaxTimePerRoute()) { validFinalInsertion = false; }
                                    } else { validFinalInsertion = false; }
                                    pre2 = suc2;
                                } while((suc2 != depot) && validFinalInsertion);
                        } else { validFinalInsertion = false; }
                    } else { validFinalInsertion = false; }

                    // Inserción factible: conservar la de menor coste para este cliente
                    if(validFinalInsertion==true) { // cliente, ruta, predecesor, coste
                        if(costInsertion < infoCandidate[3]) {
                            infoCandidate[0] = candidate; infoCandidate[1] = k; infoCandidate[2] = pre; infoCandidate[3] = costInsertion; infoCandidate[4] = this.solution.getProblem().getScore(candidate); // cliente, ruta, predecesor, coste, score
                        }
                    }

                    pre = suc;
                } while(suc != depot);
            } //rutas creadas

            // almacenamos en la lista de candidatos la mejor posición de inserción para el cliente
            if(infoCandidate[0]!=-1 && infoCandidate[1]!=-1 && infoCandidate[2]!=-1 && infoCandidate[3] != Double.MAX_VALUE && infoCandidate[4]!=-1) {
                double[] infoCandidate2 = new double[5];
                infoCandidate2[0] = infoCandidate[0];  infoCandidate2[1] = infoCandidate[1];
                infoCandidate2[2] = infoCandidate[2];  infoCandidate2[3] = infoCandidate[3];
                infoCandidate2[4] = infoCandidate[4];
                candidatesList.add(infoCandidate2);
            }
            validFinalInsertion = true;
            infoCandidate[0] = -1;  infoCandidate[1] = -1;
            infoCandidate[2] = -1;  infoCandidate[3] = Double.MAX_VALUE;
            infoCandidate[4] = -1;
        } // cliente

        return candidatesList;
    }

    /**
     * Devuelve la solución con la que trabaja el algoritmo.
     *
     * @return solución actual.
     */
    public TOPTWSolution getSolution() {
        return solution;
    }

    /**
     * Establece la solución con la que trabaja el algoritmo.
     *
     * @param solution nueva solución (asociada a la instancia del problema).
     */
    public void setSolution(TOPTWSolution solution) {
        this.solution = solution;
    }

    /**
     * Devuelve el tiempo de cómputo asociado a la solución.
     *
     * @return tiempo registrado (actualmente no se actualiza; vale 0 por defecto).
     */
    public int getSolutionTime() {
        return solutionTime;
    }

    /**
     * Establece el tiempo de cómputo asociado a la solución.
     *
     * @param solutionTime tiempo a registrar.
     */
    public void setSolutionTime(int solutionTime) {
        this.solutionTime = solutionTime;
    }

    /**
     * Obtiene la mayor puntuación entre todos los POIs de la instancia.
     *
     * Se utiliza para normalizar la función de pertenencia en las selecciones difusas.
     *
     * @return puntuación máxima (o -1.0 si no hay ninguna puntuación positiva).
     */
    public double getMaxScore() {
        double maxSc = -1.0;
        for(int i = 0; i < this.solution.getProblem().getScore().length; i++) {
            if(this.solution.getProblem().getScore(i) > maxSc)
                maxSc = this.solution.getProblem().getScore(i);
        }
        return maxSc;
    }

}