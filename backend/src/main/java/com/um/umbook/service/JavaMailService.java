package com.um.umbook.service;

import com.um.umbook.model.SolicitudAmistad;
import com.um.umbook.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de email. En la demo es un STUB: no envia mails reales (no hay SMTP
 * configurado), solo registra en el log lo que se enviaria. Metodos 1:1 con el diagrama.
 */
@Service
public class JavaMailService {

    private static final Logger log = LoggerFactory.getLogger(JavaMailService.class);

    public void enviarEmail(String destinatario, String asunto, String cuerpo) {
        log.info("[MAIL STUB] para={} asunto='{}' cuerpo='{}'", destinatario, asunto, cuerpo);
    }

    public void enviarEmailSolicitudAmistad(SolicitudAmistad solicitud) {
        enviarEmail(solicitud.getDestinatario().getEmail(),
                "Nueva solicitud de amistad",
                solicitud.getRemitente().getNombre() + " te envio una solicitud de amistad. Token: "
                        + solicitud.getTokenEmail());
    }

    public void enviarEmailCumpleanos(Usuario usuario, Usuario amigo) {
        enviarEmail(usuario.getEmail(),
                "Cumpleaños de un amigo",
                amigo.getNombre() + " " + amigo.getApellido() + " cumple años hoy!");
    }
}
