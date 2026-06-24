package com.um.umbook.seed;

import com.um.umbook.model.Amistad;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.AmistadRepository;
import com.um.umbook.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Carga inicial de datos para la demo. NO hay usuarios hardcodeados en el codigo de
 * negocio: todo se inserta en la base via este seeder al arrancar.
 *
 * Topologia de amistades (usuario de referencia = Martin):
 *   Martin -- Ana, Beto, Carla
 *   Diego  -- Ana, Beto          => 2 amigos en comun con Martin  (aparece en CU-13)
 *   Eva    -- Ana, Beto, Carla   => 3 amigos en comun con Martin  (aparece en CU-13)
 *   Fede   -- Ana                => 1 amigo en comun con Martin   (NO aparece en CU-13)
 *   Gabi   -- (sin amigos)       => 0                              (NO aparece)
 *   Tincho -- Ana, Beto, Carla   => 3 amigos en comun con Martin  (aparece en CU-13).
 *            Logueado como Tincho: ve a Martin/Diego/Eva como sugerencias y, como Ana (su
 *            amiga) cumple hoy, recibe la notificacion de cumpleaños (CU-15).
 *
 * Cumpleaños: Ana cumple hoy (para disparar CU-15 sin scripts).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final AmistadRepository amistadRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository, AmistadRepository amistadRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.amistadRepository = amistadRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return; // ya sembrado
        }

        LocalDate hoy = LocalDate.now();

        Usuario martin = crear("Martin", "Salvatore", "martin@um.edu.ar", "martin", LocalDate.of(1999, 3, 10));
        Usuario ana = crear("Ana", "Gomez", "ana@um.edu.ar", "ana", hoy.withYear(2001));        // cumple hoy
        Usuario beto = crear("Beto", "Diaz", "beto@um.edu.ar", "beto", LocalDate.of(2000, 8, 5));
        Usuario carla = crear("Carla", "Lopez", "carla@um.edu.ar", "carla", LocalDate.of(1998, 11, 22));
        Usuario diego = crear("Diego", "Ruiz", "diego@um.edu.ar", "diego", LocalDate.of(2001, 1, 30));
        Usuario eva = crear("Eva", "Mura", "eva@um.edu.ar", "eva", LocalDate.of(2002, 4, 18));
        Usuario fede = crear("Fede", "Sosa", "fede@um.edu.ar", "fede", LocalDate.of(2000, 9, 9));
        Usuario gabi = crear("Gabi", "Vega", "gabi@um.edu.ar", "gabi", LocalDate.of(1997, 12, 1));
        Usuario tincho = crear("Tincho", "Vargas", "tincho11@um.edu.ar", "tincho11", LocalDate.of(2001, 6, 7));

        // Amistades de Martin
        amistad(martin, ana);
        amistad(martin, beto);
        amistad(martin, carla);

        // Diego: 2 en comun con Martin (Ana, Beto)
        amistad(diego, ana);
        amistad(diego, beto);

        // Eva: 3 en comun con Martin (Ana, Beto, Carla)
        amistad(eva, ana);
        amistad(eva, beto);
        amistad(eva, carla);

        // Fede: 1 en comun con Martin (Ana)
        amistad(fede, ana);

        // Gabi: sin amistades

        // Tincho (tincho11): amigo de Ana, Beto, Carla => 3 en comun con Martin (aparece en CU-13).
        // Ana cumple hoy => logueado como tincho11 recibe la notificacion de cumpleaños (CU-15).
        amistad(tincho, ana);
        amistad(tincho, beto);
        amistad(tincho, carla);

        log.info("Seed completo: {} usuarios, {} amistades. Usuario de referencia demo = 'martin' (id={}).",
                usuarioRepository.count(), amistadRepository.count(), martin.getId());
    }

    private Usuario crear(String nombre, String apellido, String email, String nombreUsuario,
                          LocalDate fechaNacimiento) {
        Usuario u = new Usuario(nombre, apellido, email, nombreUsuario,
                passwordEncoder.encode("demo1234"), fechaNacimiento);
        return usuarioRepository.save(u);
    }

    private void amistad(Usuario a, Usuario b) {
        amistadRepository.save(new Amistad(a, b));
    }
}
