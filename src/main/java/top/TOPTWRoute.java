package top;

/**
 * Nodo de una ruta: guarda su predecesor y su sucesor.
 *
 * Es un contenedor sencillo (estructura de datos) para describir la posición de un nodo dentro
 * de una ruta mediante enlaces a su nodo anterior y posterior. Los valores son identificadores
 * de nodo del problema (`0` es el depósito original).
 *
 * @note Los nombres de los métodos (`getPredeccesor`, `getSuccesor`) conservan la grafía
 *       original del código.
 */
public class TOPTWRoute {
    /** Identificador del nodo predecesor en la ruta. */
    int predecessor;
    /** Identificador del nodo sucesor en la ruta. */
    int succesor;
    /** Identificador del nodo (o de la ruta) al que pertenecen estos enlaces. */
    int id;

    /** Crea un elemento de ruta con todos sus campos a 0. */
    TOPTWRoute() {

    }

    /**
     * Crea un elemento de ruta con los valores indicados.
     *
     * @param pre  identificador del nodo predecesor.
     * @param succ identificador del nodo sucesor.
     * @param id   identificador del nodo o ruta.
     */
    TOPTWRoute(int pre, int succ, int id) {
        this.predecessor = pre;
        this.succesor = succ;
        this.id = id;
    }

    /**
     * Devuelve el predecesor.
     *
     * @return identificador del nodo predecesor.
     */
    public int getPredeccesor() {
        return this.predecessor;
    }

    /**
     * Devuelve el sucesor.
     *
     * @return identificador del nodo sucesor.
     */
    public int getSuccesor() {
        return this.succesor;
    }

    /**
     * Devuelve el identificador.
     *
     * @return identificador del nodo o ruta.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Establece el predecesor.
     *
     * @param pre identificador del nuevo nodo predecesor.
     */
    public void setPredeccesor(int pre) {
        this.predecessor = pre;
    }

    /**
     * Establece el sucesor.
     *
     * @param suc identificador del nuevo nodo sucesor.
     */
    public void setSuccesor(int suc) {
        this.succesor = suc;
    }

    /**
     * Establece el identificador.
     *
     * @param id nuevo identificador del nodo o ruta.
     */
    public void setId(int id) {
        this.id = id;
    }
}