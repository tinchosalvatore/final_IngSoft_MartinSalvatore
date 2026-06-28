Procedimiento de Prueba - Martin Salvatore

CP 1.3.1 — Búsqueda por nombre

1. Dirigirse a la sección de buscador de usuarios.
2. Escribir "Ana" en la searchbar.
3. Confirmar que el sistema realiza la petición GET /usuarios/buscar?nombre=Ana&apellido=Ana.
4. Verificar que el service ejecuta buscarUsuarios("Ana", "Ana") → findByNombreContainingOrApellidoContaining("Ana", "Ana").
5. Comprobar que la lista devuelta incluye a los usuarios cuyo nombre contiene "Ana".

CP 1.3.2 — Búsqueda por apellido

1. Dirigirse a la sección de buscador de usuarios.
2. Escribir "Gomez" en la searchbar.
3. Confirmar que el sistema realiza la petición GET /usuarios/buscar?nombre=Gomez&apellido=Gomez.
4. Comprobar que la lista devuelta incluye a los usuarios cuyo apellido contiene "Gomez".

CP 1.3.3 — Búsqueda por nombre o apellido (coincidencia parcial)

1. Dirigirse a la sección de buscador de usuarios.
2. Escribir " gomez " (con espacios y en minúsculas) en la searchbar.
3. Verificar que el texto se recorta y la búsqueda es insensible a mayúsculas (lower + containing).
4. Comprobar que el sistema devuelve las coincidencias por nombre o por apellido.

CP 1.3.5 — Sin resultados

1. Dirigirse a la sección de buscador de usuarios.
2. Escribir "zzzz" en la searchbar (texto que no coincide con ningún usuario).
3. Confirmar que el sistema realiza la petición GET /usuarios/buscar?nombre=zzzz&apellido=zzzz.
4. Verificar que el backend devuelve una lista vacía (200 []).
5. Comprobar que el front muestra el mensaje "No se encontraron usuarios".
