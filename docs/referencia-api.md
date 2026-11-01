# Referencia del código fuente

Esta sección contiene la documentación técnica generada
automáticamente mediante Doxygen a partir del código Java
del proyecto RTVRPTW.

## Documentación de clases y métodos

La documentación incluye:

- Descripción de las clases principales.
- Métodos y atributos.
- Relaciones entre componentes.
- Diagramas de clases.
- Grafos de llamadas.

## Consultar la documentación completa

[Acceder a la documentación Doxygen](referencia/index.html)

## Contenido de la referencia

| Paquete               | Clases                                                                                          |
|-----------------------|-------------------------------------------------------------------------------------------------|
| `top`                 | `TOPTW`, `TOPTWSolution`, `TOPTWGRASP`, `TOPTWReader`, `TOPTWEvaluator`, `TOPTWRoute`, `mainTOPTW` |
| `exposito.utilities`  | `ExpositoUtilities`, `BellmanFord`, `Pair`, `PowerSet`                                          |

Desde la página principal de Doxygen se accede a las vistas más útiles:

| Vista                    | Para qué sirve                                                         |
|--------------------------|------------------------------------------------------------------------|
| Paquetes (*Namespaces*)  | Lista de paquetes y de sus clases                                      |
| Lista de clases          | Índice de todas las clases con su descripción breve                    |
| Jerarquía de clases      | Relaciones de herencia                                                 |
| Archivos                 | Documentación por fichero fuente                                       |
| Búsqueda                 | Búsqueda de clases, métodos y atributos por nombre                     |

En la página de cada clase se muestran, además, los **diagramas de colaboración** y, en cada
método, sus **grafos de llamadas** (qué métodos invoca y desde dónde se le llama).

## Cómo generar la referencia

Desde la raíz del proyecto:

```bash
doxygen Doxyfile
```

La salida debe quedar en `docs/referencia/` (véase [Instalación y configuración](instalacion.md#generacion-de-la-documentacion)),
de modo que el enlace anterior funcione tanto con `mkdocs serve` como con `mkdocs build`.

### Opciones relevantes del `Doxyfile`

| Opción                    | Valor                | Efecto                                                                 |
|---------------------------|----------------------|------------------------------------------------------------------------|
| `INPUT`                   | `src/main/java`      | Carpeta de código fuente analizada (de forma recursiva)                |
| `OPTIMIZE_OUTPUT_JAVA`    | `YES`                | Terminología y presentación propias de Java                            |
| `JAVADOC_AUTOBRIEF`       | `YES`                | La primera frase del comentario es la descripción breve                |
| `MARKDOWN_SUPPORT`        | `YES`                | Permite Markdown dentro de los comentarios                             |
| `EXTRACT_ALL`             | `YES`                | Documenta también los elementos sin comentario                         |
| `EXTRACT_PRIVATE`         | `NO`                 | No incluye los miembros privados                                       |
| `HAVE_DOT`                | `YES`                | Genera diagramas con Graphviz                                          |
| `CALL_GRAPH` / `CALLER_GRAPH` | `YES`            | Grafos de llamadas de cada método                                      |
| `WARN_IF_UNDOCUMENTED`    | `YES`                | Avisa de los elementos sin documentar                                  |

## Escribir comentarios para la referencia

Para que un elemento aparezca bien documentado debe llevar un comentario Javadoc inmediatamente
antes de su declaración:

```java
/**
 * Calcula la distancia total recorrida por una ruta.
 *
 * Suma las distancias entre cada par de nodos consecutivos.
 *
 * @param route secuencia ordenada de nodos visitados.
 * @return distancia total (0 si hay menos de dos nodos).
 * @throws IllegalArgumentException si la ruta es nula.
 */
public double getDistance(ArrayList<Integer> route) { ... }
```

Pautas:

- La **primera frase** (hasta el primer punto) será la descripción breve: debe ser completa y
  terminar en punto.
- Documentar **todos** los parámetros con `@param` usando el nombre exacto del código, y el
  resultado con `@return`.
- Usar `@note` para observaciones y `@warning` para advertencias de uso.
- Puede utilizarse Markdown: listas, tablas, `código` en línea y bloques con `@code ... @endcode`.
- Evitar `<` y `>` fuera de bloques de código, porque Doxygen puede interpretarlos como HTML.

!!! tip "Comprobación"
Al ejecutar `doxygen Doxyfile`, los avisos del tipo `warning: ... is not documented` o
`argument ... not found` indican comentarios incompletos o con nombres de parámetro erróneos.