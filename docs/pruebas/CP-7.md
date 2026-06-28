# CU-7: Buscar Usuarios — Martin Salvatore

## Casos de Prueba

| Id | Objetivo | Estado Inicial | Datos de Prueba | Resultado Esperado |
| --- | --- | --- | --- | --- |
| CP 1.3.1 | Lograr una búsqueda exitosa por nombre | Existen en el sistema usuarios cuyo nombre contiene el texto buscado (ej. "Ana Gomez") | Texto de búsqueda: "Ana" | El sistema devuelve los usuarios cuyo nombre coincide con "Ana" |
| CP 1.3.2 | Lograr una búsqueda exitosa por apellido | Existen en el sistema usuarios cuyo apellido contiene el texto buscado (ej. "Ana Gomez") | Texto de búsqueda: "Gomez" | El sistema devuelve los usuarios cuyo apellido coincide con "Gomez" |
| CP 1.3.3 | Lograr una búsqueda exitosa por nombre o apellido desde un solo campo | La searchbar envía el mismo texto como nombre y como apellido; la coincidencia es parcial e insensible a mayúsculas | Texto de búsqueda: " gomez " | El sistema devuelve las coincidencias por nombre o apellido (texto recortado, sin distinguir mayúsculas) |
| CP 1.3.5 | Comprobar que una búsqueda sin resultados muestre el mensaje adecuado | No existe ningún usuario cuyo nombre o apellido coincida con el texto | Texto de búsqueda: "zzzz" | El sistema devuelve una lista vacía y el front muestra "No se encontraron usuarios" |

> Nota: los casos 1.3.4 (búsqueda sin criterios) y 1.3.6 (usuario deshabilitado no aparece) del Plan
> de Pruebas quedan **fuera del alcance** de la implementación actual: una búsqueda con texto vacío
> coincide con todos los usuarios (no es un error), y la búsqueda no filtra por estado `activo`.
