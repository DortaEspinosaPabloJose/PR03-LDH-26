# Manual de usuario

Esta sección describe cómo utilizar el sistema RTVRPTW,
sus funcionalidades principales y el procedimiento
para obtener recomendaciones turísticas.

## Qué hace el sistema

Dada una instancia (conjunto de POIs con puntuaciones, ventanas de tiempo y tiempos de visita),
el sistema genera uno o varios **itinerarios**: cada ruta es una secuencia ordenada de POIs que
empieza y termina en el depósito, respeta los horarios y no supera el tiempo máximo.

!!! info "Versión actual"
El sistema se utiliza por línea de comandos sobre ficheros de instancias. Los resultados se
muestran por la salida estándar.

## Preparar las instancias

Los ficheros de instancia deben estar en la carpeta `resources/`, en la raíz del proyecto. El
programa principal procesa estas 29 instancias:

| Familia | Ficheros                | Distribución de los POIs     |
|---------|-------------------------|------------------------------|
| `c1xx`  | `c101.txt` ... `c109.txt`   | Agrupados (*clustered*)      |
| `r1xx`  | `r101.txt` ... `r112.txt`   | Aleatoria (*random*)         |
| `rc1xx` | `rc101.txt` ... `rc108.txt` | Mixta (*random-clustered*)   |

El formato de los ficheros se describe en la [Guía técnica](guia-tecnica.md#formato-de-las-instancias).

## Ejecutar el programa

Desde la raíz del proyecto, tras compilar con Maven:

```bash
java -cp target/classes top.mainTOPTW
```

Para cada instancia el programa ejecuta GRASP tres veces con **10000 iteraciones** y con listas
restringidas de candidatos de tamaño **3, 5 y 7**.

!!! warning "Volumen de salida"
En cada iteración se imprime el informe completo de la solución. Con los valores por defecto
se generan decenas de miles de informes por instancia. Se recomienda redirigir la salida a un
fichero:

    ```bash
    java -cp target/classes top.mainTOPTW > resultados.txt
    ```

## Interpretar la salida

Para cada instancia se imprime una cabecera, los informes de las soluciones y un resumen por
cada ejecución de GRASP.

### Resumen por ejecución

```text
 --> Instance: c101.txt
 ...
 --> MEDIA: <puntuación media de las soluciones construidas>
 --> MEJOR SOLUCION: <mayor puntuación obtenida>
```

- **MEDIA:** media de la puntuación total de todas las soluciones construidas.
- **MEJOR SOLUCION:** mayor puntuación total encontrada. Es el valor de referencia para
  comparar configuraciones (por ejemplo, tamaños de RCL).

### Informe de una solución

Cada informe empieza con el listado de rutas y su evaluación:

```text
SOLUTION:
0 - 5 - 2 - 9 - 0
101 - 7 - 14 - 101
FEASIBLE SOLUTION: true
SCORE: <puntuación total>
TIME COST: <tiempo total de las rutas>
```

*(Los números son ilustrativos.)*

| Elemento            | Significado                                                                         |
|---------------------|-------------------------------------------------------------------------------------|
| `0 - 5 - 2 - 9 - 0` | Una ruta: sale del depósito, visita los POIs 5, 2 y 9, y regresa al depósito        |
| `101`, `102`, ...   | Identificador del depósito de las rutas adicionales (copias del depósito `0`)       |
| `FEASIBLE SOLUTION` | `true` si se cumplen todas las ventanas de tiempo y el tiempo máximo por ruta       |
| `SCORE`             | Suma de las puntuaciones de los POIs visitados (lo que se desea **maximizar**)      |
| `TIME COST`         | Suma de los tiempos de finalización de todas las rutas                              |

A continuación se muestran los datos del problema (nodos, tiempo máximo por ruta y número máximo
de rutas) y, para cada ruta, una tabla con una fila por parada:

| Columna        | Significado                                           |
|----------------|-------------------------------------------------------|
| `CUST NO.`     | Identificador del POI                                 |
| `X COORD.`, `Y. COORD.` | Coordenadas del POI                          |
| `READY TIME`   | Apertura de la ventana de tiempo                      |
| `DUE DATE`     | Cierre de la ventana de tiempo                        |
| `ARRIVE TIME`  | Instante en que comienza la visita (tras la espera, si llegó antes de abrir) |
| ` LEAVE TIME`  | Instante en que termina la visita                     |
| `SERVICE TIME` | Tiempo de servicio                                    |

### Lectura turística de los resultados

Cada ruta equivale a un **itinerario** (por ejemplo, el de una jornada): el orden de las paradas
es el orden de visita recomendado, y `ARRIVE TIME` / `LEAVE TIME` indican a qué hora empezar y
terminar cada visita.

## Uso desde código Java

Para resolver una única instancia o integrar el algoritmo en otra aplicación:

```java
import top.*;

TOPTW problem = TOPTWReader.readProblem("resources/c101.txt");
TOPTWSolution solution = new TOPTWSolution(problem);
TOPTWGRASP grasp = new TOPTWGRASP(solution);

// Una única construcción con RCL de tamaño 5
grasp.computeGreedySolution(5);
solution.printSolution();                       // rutas y puntuación por consola
System.out.println(solution.getInfoSolution()); // informe detallado

// O bien el procedimiento GRASP completo (iteraciones, tamaño de RCL)
grasp.GRASP(1000, 5);
```

!!! note
`GRASP()` reutiliza el mismo objeto `TOPTWSolution`: al terminar, éste contiene la
**última** solución construida, no la mejor. De la mejor sólo se informa su puntuación.

## Parámetros de ejecución

| Parámetro         | Dónde se define                          | Efecto                                                                  |
|-------------------|------------------------------------------|-------------------------------------------------------------------------|
| `maxIterations`   | Argumento de `GRASP()`                   | Número de soluciones construidas. Más iteraciones, más tiempo y mejores resultados esperables |
| `maxSizeRCL`      | Argumento de `GRASP()` y `computeGreedySolution()` | Tamaño de la RCL. Valores bajos: más voraz; altos: más diversidad |
| `selection`       | Variable interna de `computeGreedySolution()` | Método de selección: 1 aleatoria, 2 difusa determinista, 3 difusa con corte alfa |
| `alpha`           | Variable interna de `computeGreedySolution()` | Umbral del corte alfa (por defecto `0.8`)                            |

!!! tip
`selection` y `alpha` están fijados en el código. Para probar otras estrategias hay que
modificar sus valores en `TOPTWGRASP.computeGreedySolution()` y volver a compilar.

## Problemas frecuentes

| Problema                                           | Qué hacer                                                         |
|----------------------------------------------------|-------------------------------------------------------------------|
| El programa termina sin resultados y muestra un error de fichero | Comprobar que se ejecuta desde la raíz y que existe `resources/` con las instancias |
| Excepción de índice al leer una instancia          | El fichero no respeta el [formato esperado](guia-tecnica.md#formato-de-las-instancias) |
| La ejecución tarda mucho                           | Reducir `maxIterations` o el número de instancias en `mainTOPTW`, y redirigir la salida a un fichero |
| `FEASIBLE SOLUTION: false`                         | El constructor valida cada inserción, por lo que lo esperable es `true`. Si aparece `false`, revisar que la instancia se leyó correctamente |