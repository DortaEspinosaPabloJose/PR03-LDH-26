package top;

/**
 * Evaluador de soluciones del TOPTW.
 *
 * Clase reservada para centralizar el cálculo de la función objetivo de una
 * `TOPTWSolution`. Actualmente el método `evaluate()` **no está implementado**
 * (el cuerpo conserva comentado código heredado de un problema CVRP acumulativo).
 * La puntuación de una solución se obtiene de momento mediante
 * `TOPTWSolution.evaluateFitness()`.
 *
 * @see TOPTWSolution#evaluateFitness()
 */
public class TOPTWEvaluator {
    /** Valor centinela que indica que una solución todavía no ha sido evaluada. */
    public static double NO_EVALUATED = -1.0;

    /**
     * Evalúa una solución y debería almacenar su valor objetivo en ella.
     *
     * @note Método sin implementar: no modifica la solución.
     *
     * @param solution solución que se desea evaluar.
     */
    public void evaluate(TOPTWSolution solution) {
        /*CumulativeCVRP problem = solution.getProblem();
        double objectiveFunctionValue = 0.0;
        for (int i = 0; i < solution.getIndexDepot().size(); i++) {
            double cumulative = 0;
            int depot = solution.getAnIndexDepot(i);
            int actual = depot;
            actual = solution.getSuccessor(actual);
            cumulative += problem.getDistanceMatrix(0, actual);
            objectiveFunctionValue += problem.getDistanceMatrix(0, actual);
            System.out.println("Desde " + 0 + " a " + actual + " = " + cumulative);
            while (actual != depot) {
                int ant = actual;
                actual = solution.getSuccessor(actual);
                if (actual != depot) {
                    cumulative += problem.getDistanceMatrix(ant, actual);
                    objectiveFunctionValue += cumulative;
                    System.out.println("Desde " + ant + " a " + actual + " = " + cumulative);
                } else {
                    cumulative += problem.getDistanceMatrix(ant, 0);
                    objectiveFunctionValue += cumulative;
                    System.out.println("Desde " + ant + " a " + 0 + " = " + cumulative);
                }
            }
            System.out.println("");
        }
        solution.setObjectiveFunctionValue(objectiveFunctionValue);*/
    }
}