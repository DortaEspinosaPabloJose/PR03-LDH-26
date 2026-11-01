# Guía técnica

Esta guía describe los aspectos técnicos del proyecto,
su estructura interna, sus componentes y su funcionamiento.

Para consultar las clases y métodos se dispone
de la referencia Doxygen.

## Modelo del problema

### Elementos

Una instancia del TOPTW (clase `TOPTW`) contiene:

| Dato                    | Descripción                                                                 |
|-------------------------|-----------------------------------------------------------------------------|
| `nodes`                 | Número de POIs, sin contar el depósito                                      |
| `x`, `y`                | Coordenadas de cada nodo                                                    |
| `score`                 | Puntuación (interés) de cada nodo                                           |
| `readyTime`, `dueTime`  | Apertura y cierre de la ventana de tiempo de cada nodo                      |
| `serviceTime`           | Duración de la visita a cada nodo                                           |
| `vehicles`              | Número de vehículos (rutas) disponibles                                     |
| `maxTimePerRoute`       | Duración máxima de una ruta (cierre de la ventana de tiempo del depósito)   |
| `distanceMatrix`        | Matriz simétrica de distancias euclídeas                                    |

### Indexación

- El nodo `0` es el depósito; los nodos `1..nodes` son los POIs.
- Los vectores tienen tamaño `nodes + 1`.
- Los identificadores **mayores que `nodes`** son copias del depósito, una por ruta adicional, y
  se traducen al nodo `0` en todas las consultas.

### Distancia y tiempo de viaje

La distancia es euclídea y el tiempo de viaje es igual a la distancia (velocidad unitaria):

```text
d(i, j) = raíz( (x_i - x_j)^2 + (y_i - y_j)^2 )
```

### Función objetivo y restricciones

- **Objetivo:** maximizar la suma de las puntuaciones de los POIs incluidos en las rutas
  (`TOPTWSolution.evaluateFitness()`).
- **Cada POI se visita como máximo una vez**, en una sola ruta.
- **Ventanas de tiempo:** la llegada a un POI debe ser anterior a su cierre. Si se llega antes de
  la apertura, se espera.
- **Tiempo máximo:** ningún instante de una ruta, incluido el regreso al depósito, puede superar
  `maxTimePerRoute`.
- **Número de rutas:** no se pueden crear más rutas que vehículos.

## Formato de las instancias

`TOPTWReader.readProblem()` interpreta ficheros de texto en los que los campos se separan por
espacios o tabuladores (se normalizan con `ExpositoUtilities.simplifyString()`).

1. **Primera línea:** cabecera. El campo de índice 1 es el número de vehículos y el de índice 2 el
   número de POIs.
2. **Segunda línea:** se descarta.
3. **`nodes + 1` líneas siguientes:** una por nodo, comenzando por el depósito.

Columnas de cada línea de nodo (índice de campo, empezando en 0):

| Campo  | Contenido                                                        |
|:------:|------------------------------------------------------------------|
| 1      | Coordenada X                                                     |
| 2      | Coordenada Y                                                     |
| 3      | Tiempo de servicio                                               |
| 4      | Puntuación                                                       |
| 7 y 8  | Apertura y cierre de la ventana (**sólo en el depósito**)        |
| 8 y 9  | Apertura y cierre de la ventana (resto de nodos)                 |

Tras la lectura se calcula la matriz de distancias y se fija `maxTimePerRoute` igual al cierre
de la ventana del depósito.

## Algoritmo GRASP

### Esquema general

```text
procedimiento GRASP(maxIteraciones, tamañoRCL)
    para k = 1 .. maxIteraciones
        solución <- ConstrucciónVorazAleatorizada(tamañoRCL)
        solución <- BúsquedaLocal(solución)          [pendiente]
        actualizar media y mejor valor
    fin
```

Implementado en `TOPTWGRASP.GRASP()`. De cada iteración se calcula la puntuación
(`evaluateFitness()`), se acumula para la media y se conserva el mejor valor. No se guarda la
mejor solución, sólo su puntuación.

### Construcción voraz aleatorizada

`TOPTWGRASP.computeGreedySolution()` construye una solución completa:

1. **Inicialización:** `initSolution()` deja una única ruta vacía (ruta 0). Todos los POIs
   quedan en la lista de clientes libres.
2. **Evaluación:** `comprehensiveEvaluation()` calcula, para cada cliente libre, su mejor
   posición de inserción factible. Los candidatos se ordenan por **coste ascendente**.
3. **Lista restringida (RCL):** se toman los `maxSizeRCL` mejores candidatos (o todos, si hay
   menos).
4. **Selección:** se elige un candidato de la RCL según el método configurado.
5. **Inserción:** `updateSolution()` enlaza el cliente en la ruta y actualiza los tiempos de
   salida de los nodos posteriores.
6. **Reevaluación:** se repite desde el paso 2 con los clientes que quedan.
7. **Nueva ruta:** si no existe ningún candidato factible, se crea otra ruta con `addRoute()`
   mientras queden vehículos. Si no quedan, la construcción termina.

El **criterio voraz** es el coste temporal de la inserción y la **aleatorización** procede de
elegir dentro de la RCL, no siempre el primero.

### Candidatos

Cada candidato es un vector `double[5]`:

| Índice | Contenido                                                               |
|:------:|-------------------------------------------------------------------------|
| 0      | Cliente (POI) a insertar                                                |
| 1      | Índice de la ruta                                                       |
| 2      | Nodo predecesor tras el cual se inserta                                 |
| 3      | Coste: instante en que termina la ruta tras la inserción                |
| 4      | Puntuación del cliente                                                  |

### Comprobación de factibilidad de una inserción

Para insertar un cliente `c` entre `pre` y `suc` se simulan los tiempos de la ruta:

```text
llegada(c)   = salida(pre) + d(pre, c)         debe cumplir  llegada(c) < cierre(c)
inicio(c)    = max(llegada(c), apertura(c))    (espera si se llega antes de abrir)
salida(c)    = inicio(c) + servicio(c)         debe cumplir  salida(c) <= maxTimePerRoute
```

Después se **propaga** el efecto al sucesor y a todos los nodos siguientes de la ruta, hasta el
depósito, aplicando las mismas reglas:

```text
llegada(suc) = salida(c) + d(c, suc)           debe cumplir  llegada(suc) < cierre(suc)
...                                            y así hasta volver al depósito
```

Si en cualquier punto se incumple una condición, la inserción es **no factible** y se descarta.
Si es factible, su coste es el instante final de la ruta (la salida del depósito de cierre).
Para cada cliente se conserva la posición factible de menor coste entre todas las rutas.

!!! note "Comparación estricta"
La condición de cierre de la ventana usa una comparación estricta (`<`), de modo que llegar
exactamente en el instante de cierre se considera no factible. El límite `maxTimePerRoute`
se comprueba con `>`, por lo que alcanzarlo exactamente sí es válido.

### Métodos de selección en la RCL

| Valor de `selection` | Método                                | Descripción                                                                              |
|:--------------------:|---------------------------------------|------------------------------------------------------------------------------------------|
| 1                    | `aleatorySelectionRCL()`              | Elección uniforme al azar entre los elementos de la RCL                                  |
| 2                    | `fuzzySelectionBestFDRCL()`           | Elige el de mayor puntuación (menor grado de pertenencia `1 - score / maxScore`)         |
| 3 *(por defecto)*    | `fuzzySelectionAlphaCutRCL()`         | Conserva los candidatos con grado de pertenencia `<= alpha` y elige uno al azar          |

En el método 3, con `alpha = 0.8` se aceptan los candidatos cuya puntuación es al menos el 20 %
de la máxima de la instancia. Si ninguno cumple el corte, se elige al azar entre toda la RCL.

La selección combina así dos criterios: la **RCL** se forma por coste temporal (inserciones
baratas) y la **selección difusa** prioriza la puntuación.

### Parámetros

| Parámetro        | Valor por defecto               | Dónde se modifica                                  |
|------------------|---------------------------------|----------------------------------------------------|
| `maxIterations`  | 10000                           | Llamada a `GRASP()` en `mainTOPTW`                 |
| `maxSizeRCL`     | 3, 5 y 7 (tres ejecuciones)     | Llamada a `GRASP()` en `mainTOPTW`                 |
| `selection`      | 3                               | Variable local en `computeGreedySolution()`        |
| `alpha`          | 0.8                             | Variable local en `computeGreedySolution()`        |

## Informes de solución

`TOPTWSolution.getInfoSolution()` recorre cada ruta, recalcula los instantes de llegada y salida
y valida la solución (ventanas de tiempo y tiempo máximo). Devuelve un texto con:

- Un resumen: rutas, factibilidad, puntuación total y coste temporal total.
- Los datos del problema.
- Una tabla por ruta con coordenadas, ventana de tiempo e instantes de llegada y salida.

`TOPTWSolution.printSolution()` muestra únicamente las rutas y su puntuación (`SC=`).

## Utilidades

Paquete `exposito.utilities`:

| Clase                | Función                                                                                   |
|----------------------|-------------------------------------------------------------------------------------------|
| `ExpositoUtilities`  | Normalización de cadenas, formateo de tablas por consola, escritura de ficheros, producto de matrices, detección de ciclos en grafos |
| `BellmanFord`        | Camino mínimo desde el nodo 0 al último nodo con costes enteros (admite negativos)        |
| `Pair`               | Par genérico inmutable con `equals()` y `hashCode()`                                      |
| `PowerSet`           | Iterable sobre todos los subconjuntos de un conjunto                                      |

## Complejidad y rendimiento

El coste dominante es `comprehensiveEvaluation()`. En cada reevaluación se recorre, para cada
cliente libre, todas las posiciones de inserción de todas las rutas y, para cada posición, se
propagan los tiempos hasta el final de la ruta. Esta reevaluación se repite tras cada inserción.

- El coste crece con el número de POIs y con la longitud de las rutas.
- El tiempo total es proporcional al número de iteraciones de GRASP.
- La impresión del informe completo en cada iteración suele ser un coste relevante; para
  medir tiempos de cómputo conviene desactivarla.

## Limitaciones conocidas y trabajo futuro

| Ámbito                | Limitación                                                                                             |
|-----------------------|--------------------------------------------------------------------------------------------------------|
| Algoritmo             | La **búsqueda local** no está implementada: sólo se ejecuta la fase constructiva                       |
| Algoritmo             | No se conserva la mejor solución, sólo su puntuación; tras `GRASP()` el objeto contiene la última      |
| Evaluación            | `TOPTWEvaluator.evaluate()` está sin implementar; la puntuación se calcula en `TOPTWSolution`          |
| Configuración         | `selection` y `alpha` están fijados en el código                                                       |
| Entrada               | Un error de lectura finaliza el programa con `System.exit(0)`                                          |
| Modelo                | `setNodes()` y `addNode()` no redimensionan los vectores internos                                      |
| Informes              | `TOPTW.toString()` omite el último POI al listar los nodos                                             |
| Informes              | En `getInfoSolution()`, la columna `SERVICE TIME` muestra el tiempo de servicio del nodo anterior      |
| Datos                 | Sólo se admiten instancias en formato de Solomon; no hay carga de POIs turísticos reales               |

## Convenciones de documentación del código

Los comentarios del código fuente alimentan la [Referencia del código](referencia-api.md):

- Formato Javadoc (`/** ... */`) en español. La primera frase de cada comentario se usa como
  descripción breve (`JAVADOC_AUTOBRIEF = YES`).
- Se admite **Markdown** dentro de los comentarios (listas, tablas, bloques de código).
- Etiquetas habituales: `@param`, `@return`, `@throws`, `@see`, `@note` y `@warning`.
- Los nombres de `@param` deben coincidir con los del código para evitar avisos de Doxygen.