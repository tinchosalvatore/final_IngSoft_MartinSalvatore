import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Script de demo (CU-14): envia una solicitud de amistad REAL contra el endpoint de dominio
 * (POST /solicitudes). El backend la persiste y publica un evento; el subsistema de
 * notificaciones reacciona y la UI muestra el toast en vivo. No hay "trigger" de notificacion:
 * la notificacion ocurre porque pasa el hecho real.
 *
 * Uso (Java 21, single-file, no requiere compilar):
 *   java scripts/TriggerSolicitud.java
 *   java scripts/TriggerSolicitud.java <remitenteId> <destinatarioId>
 *
 * Por defecto: remitente=7 (fede), destinatario=1 (martin, el usuario observado).
 * Requiere el backend corriendo en http://localhost:8080.
 */
public class TriggerSolicitud {

    private static final String BASE = "http://localhost:8080";

    public static void main(String[] args) throws Exception {
        String url = BASE + "/solicitudes";
        if (args.length >= 2) {
            url += "?remitenteId=" + args[0] + "&destinatarioId=" + args[1];
        } else if (args.length == 1) {
            url += "?remitenteId=" + args[0];
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
            if (response.statusCode() / 100 == 2) {
                System.out.println(">> Mira la UI: deberia aparecer el toast de solicitud de amistad.");
            }
        } catch (Exception e) {
            System.err.println("Error: no se pudo conectar al backend en " + BASE);
            System.err.println("Levantalo con: cd backend && ./mvnw spring-boot:run");
            throw e;
        }
    }
}
