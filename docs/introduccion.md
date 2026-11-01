# Introducción

RTVRPTW es un Recomendador Turístico con Ventanas de Tiempo.

El proyecto tiene como finalidad generar recomendaciones
e itinerarios turísticos considerando las restricciones
temporales asociadas a las actividades y los usuarios.

## El problema que resuelve

Un turista dispone de un tiempo limitado y de muchos puntos de interés (POI) posibles: museos,
monumentos, restaurantes, miradores... No puede visitarlos todos, porque cada uno tiene un
**horario de apertura**, una **duración de visita** y exige un **desplazamiento**. La pregunta
es qué POIs elegir y en qué orden visitarlos para aprovechar al máximo el tiempo.

Este problema se formaliza como el *Team Orienteering Problem with Time Windows* (**TOPTW**),
una variante del problema de rutas de vehículos en la que no es obligatorio visitar todos los
nodos, sino **seleccionar** los más valiosos:

| Concepto turístico                         | Elemento del modelo                              |
|--------------------------------------------|--------------------------------------------------|
| Punto de interés                           | Nodo (cliente) con una **puntuación** (*score*)  |
| Interés del POI para el usuario            | Puntuación del nodo                              |
| Horario de apertura del POI                | Ventana de tiempo `[readyTime, dueTime]`         |
| Duración de la visita                      | Tiempo de servicio (`serviceTime`)               |
| Desplazamiento entre POIs                  | Distancia euclídea (tiempo de viaje = distancia) |
| Alojamiento / punto de partida y regreso   | Depósito (nodo `0`)                              |
| Tiempo disponible en una jornada           | Duración máxima de la ruta                       |
| Número de jornadas o itinerarios           | Número de rutas (vehículos)                      |

El **objetivo** es construir, como máximo, `k` rutas que maximicen la suma de puntuaciones de
los POIs visitados, de forma que cada visita comience dentro de su ventana de tiempo y cada ruta
termine antes del límite de tiempo.

## Enfoque de resolución

El proyecto resuelve el TOPTW con **GRASP** (*Greedy Randomized Adaptive Search Procedure*), una
metaheurística que repite dos fases:

1. **Construcción voraz aleatorizada:** se inserta un POI cada vez, eligiéndolo entre una lista
   restringida de los mejores candidatos (RCL).
2. **Búsqueda local:** mejora de la solución construida *(pendiente de implementar)*.

La selección dentro de la RCL puede ser aleatoria o **difusa** (basada en la puntuación de
cada candidato). Los detalles se explican en la [Guía técnica](guia-tecnica.md).

## Estado actual del proyecto

!!! info "Prototipo de investigación"
La versión actual es un programa de línea de comandos que resuelve instancias del banco de
pruebas estándar (familias `c1xx`, `r1xx` y `rc1xx`, derivadas de los problemas de Solomon).
Todavía no incluye interfaz gráfica ni carga de datos turísticos reales.

| Componente                              | Estado                    |
|-----------------------------------------|---------------------------|
| Lectura de instancias                   | Implementado              |
| Modelo del problema y de la solución    | Implementado              |
| Construcción GRASP (aleatoria y difusa) | Implementado              |
| Búsqueda local                          | Pendiente                 |
| Evaluador de soluciones (`TOPTWEvaluator`) | Pendiente              |
| Interfaz de usuario / datos reales      | Fuera del alcance actual  |

## Organización de esta documentación

| Sección                                        | Contenido                                                       |
|------------------------------------------------|-----------------------------------------------------------------|
| [Instalación y configuración](instalacion.md)  | Requisitos, compilación y generación de la documentación        |
| [Manual de usuario](manual-usuario.md)         | Ejecución, parámetros e interpretación de resultados            |
| [Arquitectura](arquitectura.md)                | Paquetes, clases, diagramas y flujo de ejecución                |
| [Guía técnica](guia-tecnica.md)                | Modelo, algoritmo, factibilidad, complejidad y limitaciones     |
| [Referencia del código](referencia-api.md)     | Documentación generada con Doxygen                              |