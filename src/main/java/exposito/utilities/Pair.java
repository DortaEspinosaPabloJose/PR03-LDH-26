package exposito.utilities;
import java.util.Objects;

/**
 * Par inmutable de dos valores de tipos posiblemente distintos.
 *
 * Útil para devolver dos resultados desde un método o para usar pares como claves en
 * colecciones, ya que implementa `equals()` y `hashCode()`.
 *
 * ### Ejemplo
 * @code
 * Pair<String, Integer> p = Pair.create("POI", 7);
 * String name = p.first;
 * int id = p.second;
 * @endcode
 *
 * @tparam F tipo del primer elemento.
 * @tparam S tipo del segundo elemento.
 */
public class Pair<F, S> {
    /** Primer elemento del par. */
    public final F first;
    /** Segundo elemento del par. */
    public final S second;

    /**
     * Crea un par con los elementos indicados (se admiten valores `null`).
     *
     * @param first  primer elemento.
     * @param second segundo elemento.
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Compara este par con otro objeto.
     *
     * @param o objeto con el que comparar.
     * @return `true` si `o` es un `Pair` cuyos dos elementos son iguales a los de este par.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair<?, ?>) o;
        return Objects.equals(p.first, first) && Objects.equals(p.second, second);
    }

    /**
     * Calcula el código hash del par combinando con XOR los de sus elementos.
     *
     * @note Al usar XOR, los pares `(a, b)` y `(b, a)` tienen el mismo hash (siguen
     *       siendo distintos según `equals()`).
     *
     * @return código hash del par; los elementos `null` aportan 0.
     */
    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }

    /**
     * Método de fábrica que infiere los tipos del par.
     *
     * @param a primer elemento.
     * @param b segundo elemento.
     * @return nuevo par `(a, b)`.
     * @tparam A tipo del primer elemento.
     * @tparam B tipo del segundo elemento.
     */
    public static <A, B> Pair <A, B> create(A a, B b) {
        return new Pair<A, B>(a, b);
    }
}