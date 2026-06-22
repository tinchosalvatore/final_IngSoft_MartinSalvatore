import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Script de demo (CU-15): setea el cumpleaños de un usuario a HOY y corre el batch,
 * para que la UI muestre el toast de cumpleaños en vivo a sus amigos.
 *
 * Uso (Java 21, single-file, no requiere compilar):
 *   java scripts/TriggerCumple.java
 *   java scripts/TriggerCumple.java <usuarioId>
 *
 * Por defecto: usuario=3 (beto, amigo de martin). El toast lo recibe martin (id=1).
 * Requiere el backend corriendo en http://localhost:8080.
 */
public class TriggerCumple {

    private static final String BASE = "http://localhost:8080";

    public static void main(String[] args) throws Exception {
        String url = BASE + "/dev/trigger-cumple";
        if (args.length >= 1) {
            url += "?usuarioId=" + args[0];
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        System.out.println("POST " + url);
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("HTTP " + response.statusCode());
            System.out.println(response.body());
            if (response.statusCode() == 200) {
                System.out.println(">> Mira la UI: deberia aparecer el toast de cumpleaños.");
            }
        } catch (Exception e) {
            System.err.println("Error: no se pudo conectar al backend en " + BASE);
            System.err.println("Levantalo con: cd backend && ./mvnw spring-boot:run");
            throw e;
        }
    }
}
