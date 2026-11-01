package exposito.utilities;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Iterable que recorre todos los subconjuntos (conjunto potencia) de un conjunto dado.
 *
 * Para un conjunto de `n` elementos genera los `2^n` subconjuntos, empezando por el vacío.
 * Internamente usa un contador binario (`BitSet`) en el que cada bit indica si el elemento
 * correspondiente forma parte del subconjunto actual.
 *
 * ### Ejemplo
 * @code
 * Set<Integer> base = new TreeSet<>(Arrays.asList(1, 2, 3));
 * for (Set<Integer> subset : new PowerSet<>(base)) {
 *     System.out.println(subset);   // [], [1], [2], [1, 2], [3], [1, 3], [2, 3], [1, 2, 3]
 * }
 * @endcode
 *
 * @note Cada subconjunto devuelto es un `TreeSet`, por lo que los elementos deben ser
 *       comparables entre sí (`Comparable`); de lo contrario se lanzará `ClassCastException`.
 *       El número de subconjuntos crece exponencialmente con el tamaño del conjunto.
 *
 * @tparam E tipo de los elementos del conjunto.
 */
public class PowerSet<E> implements Iterable<Set<E>> {

    /** Copia en forma de vector de los elementos del conjunto original. */
    private final E[] arr;

    /**
     * Crea el conjunto potencia de un conjunto.
     *
     * @param set conjunto base cuyos subconjuntos se quieren recorrer.
     */
    @SuppressWarnings("unchecked")
    public PowerSet(Set<E> set) {
        this.arr = (E[]) set.toArray();
    }

    /**
     * Devuelve un iterador sobre todos los subconjuntos.
     *
     * @return nuevo iterador que comienza en el subconjunto vacío.
     */
    @Override
    public Iterator<Set<E>> iterator() {
        return new PowerSetIterator();
    }

    /**
     * Iterador interno que enumera los subconjuntos incrementando un contador binario.
     */
    private class PowerSetIterator implements Iterator<Set<E>> {

        /** Contador binario; el bit `i` activo significa que `arr[i]` está en el subconjunto. */
        private final BitSet bset = new BitSet(arr.length + 1);

        /**
         * Indica si quedan subconjuntos por devolver.
         *
         * @return `false` cuando el contador desborda (se activa el bit `arr.length`).
         */
        @Override
        public boolean hasNext() {
            return !this.bset.get(arr.length);
        }

        /**
         * Devuelve el siguiente subconjunto y avanza el contador.
         *
         * @return nuevo subconjunto (`TreeSet`) con los elementos seleccionados por el contador.
         * @throws NoSuchElementException si ya se han devuelto todos los subconjuntos.
         */
        @Override
        public Set<E> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            Set<E> returnSet = new TreeSet<>();
            for (int i = 0; i < arr.length; i++) {
                if (this.bset.get(i)) {
                    returnSet.add(arr[i]);
                }
            }
            this.increment();
            return returnSet;
        }

        /** Suma 1 al contador binario. */
        private void increment() {
            for (int i = 0; i <= arr.length; i++) {
                if (this.bset.get(i)) {
                    this.bset.clear(i);
                } else {
                    this.bset.set(i);
                    return;
                }
            }
        }
    }
}