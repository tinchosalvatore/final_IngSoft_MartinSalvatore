package com.um.umbook.repository;

import com.um.umbook.model.Amistad;
import com.um.umbook.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de amistades. Metodos 1:1 con el diagrama de clases.
 * El par usuario1-usuario2 es no ordenado, por eso las consultas contemplan ambos lados.
 */
@Repository
public interface AmistadRepository extends JpaRepository<Amistad, Long> {

    Amistad findByUsuario1AndUsuario2(Usuario usuario1, Usuario usuario2);

    List<Amistad> findByUsuario1OrUsuario2(Usuario usuario1, Usuario usuario2);

    /**
     * Amigos en comun entre dos usuarios: usuarios que son amigos de u1 y tambien de u2.
     */
    @Query("""
        select c from Usuario c
        where c <> :u1 and c <> :u2
        and exists (
            select 1 from Amistad a1
            where (a1.usuario1 = :u1 and a1.usuario2 = c)
               or (a1.usuario2 = :u1 and a1.usuario1 = c)
        )
        and exists (
            select 1 from Amistad a2
            where (a2.usuario1 = :u2 and a2.usuario2 = c)
               or (a2.usuario2 = :u2 and a2.usuario1 = c)
        )
        """)
    List<Usuario> findAmigosEnComun(@Param("u1") Usuario u1, @Param("u2") Usuario u2);
}
