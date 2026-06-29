import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Script de demo (CU-15): corre el batch REAL de cumpleaños (POST /cumpleanos/ejecutar-batch),
 * la misma operacion que normalmente se agendaria a diario. El batch detecta quienes cumplen
 * hoy y, por llamada directa, notifica a sus amigos (toast en vivo). No hay "trigger" de
 * notificacion: la notificacion ocurre porque el batch detecta el hecho.
 *
 * Si se pasa un usuarioId, primero edita su cumpleaños a HOY (accion real de perfil,
 * PUT /usuarios/{id}/cumpleanos) y despues corre el batch, para elegir el cumpleañero.
 * Sin argumentos, corre el batch tal cual: el seed deja a Ana (id=2) cumpliendo hoy, y como
 * Ana es amiga de martin (el usuario observado de la demo), el toast le llega a martin.
 * Para elegir otro cumpleañero que notifique a martin, pasale un amigo suyo: 2 (ana),
 * 3 (beto) o 4 (carla). Ej: java scripts/TriggerCumple.java 3
 *
 * Uso (Java 21, single-file, no requiere compilar):
 *   java scripts/TriggerCumple.java
 *   java scripts/TriggerCumple.java <usuarioId>
 *
 * Requiere el backend corriendo en http://localhost:8080.
 */
public class TriggerCumple {

    private static final String BASE = "http://localhost:8080";

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        try {
            // Paso opcional: elegir cumpleañero editando su fecha de nacimiento a hoy.
            if (args.length >= 1) {
                String urlEdit = BASE + "/usuarios/" + args[0] + "/cumpleanos";
                HttpRequest edit = HttpRequest.newBuilder()
                        .uri(URI.create(urlEdit))
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build();
                System.out.println("PUT " + urlEdit);
                HttpResponse<String> rEdit = client.send(edit, HttpResponse.BodyHandlers.ofString());
                System.out.println("HTTP " + rEdit.statusCode() + " " + rEdit.body());
            }

            // Operacion real: el batch diario de cumpleaños.
            String urlBatch = BASE + "/cumpleanos/ejecutar-batch";
            HttpRequest batch = HttpRequest.newBuilder()
                    .uri(URI.create(urlBatch))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            System.out.println("POST " + urlBatch);
            HttpResponse<String> response = client.send(batch, HttpResponse.BodyHandlers.ofString());
            System.out.println("HTTP " + response.statusCode());
            System.out.println(response.body());
            if (response.statusCode() / 100 == 2) {
                System.out.println(">> Mira la UI: deberia aparecer el toast de cumpleaños.");
            }
        } catch (Exception e) {
            System.err.println("Error: no se pudo conectar al backend en " + BASE);
            System.err.println("Levantalo con: cd backend && ./mvnw spring-boot:run");
            throw e;
        }
    }
}
