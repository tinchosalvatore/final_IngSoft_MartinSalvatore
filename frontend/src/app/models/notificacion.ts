/** Notificacion recibida por SSE (espeja NotificacionDTO.java). */
export interface Notificacion {
  id: number;
  tipo: 'SOLICITUD_AMISTAD' | 'CUMPLEANOS' | string;
  mensaje: string;
  referenciaId: number;
  leida: boolean;
  fechaCreacion: string;
}

/** Solicitud de amistad pendiente (espeja SolicitudAmistadDTO.java). */
export interface SolicitudAmistad {
  id: number;
  remitenteNombre: string;
  remitenteUsuario: string;
  estado: string;
  fechaEnvio: string;
  /** Token de email: se usa para aceptar/rechazar (flujo por token del diagrama de clases). */
  tokenEmail: string;
}
