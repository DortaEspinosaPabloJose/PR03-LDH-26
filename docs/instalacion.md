# Instalación y configuración

## Requisitos

- Java.
- Maven.

### Detalle de versiones y herramientas

| Herramienta            | Para qué se usa                              | Observaciones                                                      |
|------------------------|----------------------------------------------|--------------------------------------------------------------------|
| JDK 8 o superior       | Compilar y ejecutar el código                | El código usa *try-with-resources* y el operador diamante (`<>`)   |
| Maven 3.x              | Construcción del proyecto                    | Se ejecuta desde la raíz, donde está el `pom.xml`                  |
| Doxygen                | Generar la referencia del código             | Versión reciente: el `Doxyfile` usa opciones de la serie 1.14 o posterior |
| Graphviz (`dot`)       | Diagramas de clases y grafos de llamadas     | El `Doxyfile` tiene `HAVE_DOT = YES`                               |
| Python 3 y MkDocs      | Construir este sitio de documentación        | Con el tema *Material* (ver `requirements.txt`)                    |

!!! note
Para compilar y ejecutar el programa sólo hacen falta Java y Maven. Doxygen, Graphviz y
MkDocs son necesarios únicamente para generar la documentación.

## Estructura del proyecto

```text
RTVRPTW/
├── pom.xml
├── Doxyfile
├── mkdocs.yml
├── requirements.txt
├── resources/                   # Instancias del problema (c101.txt, r101.txt, rc101.txt, ...)
├── src/main/java/
│   ├── top/                     # Modelo, algoritmo GRASP y programa principal
│   └── exposito/utilities/      # Utilidades generales
└── docs/                        # Fuentes de MkDocs
    ├── *.md
    └── referencia/              # HTML de Doxygen (generado)
```

## Construcción

Desde la raíz del proyecto ejecutar:

    mvn clean install

## Ejecución

El programa lee las instancias con la ruta relativa `resources/<fichero>.txt`, por lo que debe
lanzarse **desde la raíz del proyecto**:

```bash
java -cp target/classes top.mainTOPTW
```

Si se prefiere ejecutar el `.jar` generado por Maven, sustituir `target/classes` por la ruta
del fichero `.jar` de `target/`. El uso del programa se describe en el
[Manual de usuario](manual-usuario.md).

## Generación de la documentación

### 1. Referencia del código (Doxygen)

El `Doxyfile` toma el código de `src/main/java` y escribe la salida dentro de `docs`
(`OUTPUT_DIRECTORY = docs`). Para que el enlace de la
[Referencia del código](referencia-api.md) funcione, el HTML debe quedar en
`docs/referencia/`. Para ello, cambie en el `Doxyfile`:

```ini
HTML_OUTPUT            = referencia
```

!!! warning "Valor actual"
Con el valor por defecto (`HTML_OUTPUT = html`) Doxygen genera `docs/html/`, y el enlace
`referencia/index.html` no se resolvería.

Después ejecute, desde la raíz:

```bash
doxygen Doxyfile
```

Los avisos sobre elementos sin documentar aparecen en la consola (`WARN_IF_UNDOCUMENTED = YES`).

### 2. Sitio MkDocs

Instalación de las dependencias (se recomienda un entorno virtual):

```bash
pip install -r requirements.txt
```

Servidor local con recarga automática, en <http://127.0.0.1:8000>:

```bash
mkdocs serve
```

Construcción del sitio estático en la carpeta `site/`:

```bash
mkdocs build
```

!!! tip "Orden recomendado"
Ejecute primero `doxygen Doxyfile` y después `mkdocs build`: MkDocs copia el contenido de
`docs/referencia/` al sitio final junto con el resto de páginas.

## Solución de problemas

| Síntoma                                              | Causa probable y solución                                                         |
|------------------------------------------------------|-----------------------------------------------------------------------------------|
| `FileNotFoundException` al ejecutar                  | Se lanzó fuera de la raíz o falta `resources/`. Ejecutar desde la raíz del proyecto. |
| Diagramas de Doxygen ausentes                        | Graphviz no está instalado o `dot` no está en el `PATH`.                          |
| El enlace a la referencia da 404                     | Falta ejecutar Doxygen o `HTML_OUTPUT` no es `referencia`.                        |
| Los diagramas Mermaid aparecen como texto            | Falta la configuración `pymdownx.superfences` de `mkdocs.yml` o el tema *Material*. |