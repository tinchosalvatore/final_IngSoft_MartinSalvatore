package com.um.umbook.repository;

import com.um.umbook.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de usuarios. Metodos 1:1 con el diagrama de clases de diseño.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByEmail(String email);

    Usuario findByNombreUsuario(String nombreUsuario);

    /**
     * Busqueda por nombre o apellido (contiene, case-insensitive). Nombre 1:1 con el diagrama;
     * el lower(...) mantiene el comportamiento case-insensitive de la searchbar.
     */
    @Query("""
        select u from Usuario u
        where lower(u.nombre) like lower(concat('%', :nombre, '%'))
           or lower(u.apellido) like lower(concat('%', :apellido, '%'))
        """)
    List<Usuario> findByNombreContainingOrApellidoContaining(@Param("nombre") String nombre,
                                                             @Param("apellido") String apellido);

    /**
     * CU-13: usuarios con al menos 2 amigos en comun con :usuario ("personas que quizas conozcas"),
     * excluyendo al propio usuario y a sus amigos directos. Metodo 1:1 con el diagrama de clases.
     */
    @Query("""
        select c from Usuario c
        where c <> :usuario
        and not exists (
            select 1 from Amistad af
            where (af.usuario1 = :usuario and af.usuario2 = c)
               or (af.usuario2 = :usuario and af.usuario1 = c)
        )
        and (
            select count(amigo) from Usuario amigo
            where amigo <> :usuario and amigo <> c
            and exists (
                select 1 from Amistad a1
                where (a1.usuario1 = :usuario and a1.usuario2 = amigo)
                   or (a1.usuario2 = :usuario and a1.usuario1 = amigo)
            )
            and exists (
                select 1 from Amistad a2
                where (a2.usuario1 = c and a2.usuario2 = amigo)
                   or (a2.usuario2 = c and a2.usuario1 = amigo)
            )
        ) >= 2
        """)
    List<Usuario> findUsuariosConAmigosEnComun(@Param("usuario") Usuario usuario);
}
