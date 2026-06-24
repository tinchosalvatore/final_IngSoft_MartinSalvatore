import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, NgZone } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { API_URL } from '../api';
import { Notificacion, SolicitudAmistad } from '../models/notificacion';
import { SesionService } from './sesion.service';

/** Usuario por defecto de la demo (martin, id=1) cuando todavia no hay sesion. */
const DEMO_USUARIO_ID = 1;

/**
 * Cliente de notificaciones. Abre un EventSource (SSE) al backend para el usuario logueado
 * (CU-2) y emite cada notificacion que llega en vivo (CU-14 y CU-15). Tambien consulta
 * pendientes/no-leidas.
 */
@Injectable({ providedIn: 'root' })
export class NotificacionService {

  private eventSource?: EventSource;
  private notificacionesSubject = new Subject<Notificacion>();

  /** Stream de notificaciones entrantes (para el ToastComponent). */
  notificaciones$ = this.notificacionesSubject.asObservable();

  constructor(private http: HttpClient, private zone: NgZone, private sesion: SesionService) {}

  /** Id del usuario actual: el logueado o, si no hay sesion, el demo. */
  private usuarioActualId(): number {
    return this.sesion.usuarioId ?? DEMO_USUARIO_ID;
  }

  /** Abre la conexion SSE (idempotente) para el usuario actual. */
  conectar(): void {
    if (this.eventSource) {
      return;
    }
    this.eventSource = new EventSource(`${API_URL}/notificaciones/stream?usuarioId=${this.usuarioActualId()}`);
    this.eventSource.addEventListener('notificacion', (e: MessageEvent) => {
      const notificacion: Notificacion = JSON.parse(e.data);
      // EventSource corre fuera de la zona de Angular: re-entrar para disparar el render.
      this.zone.run(() => this.notificacionesSubject.next(notificacion));
    });
  }

  /** Reabre la conexion SSE con el usuario actual (tras login/logout). */
  reconectar(): void {
    this.desconectar();
    this.conectar();
  }

  desconectar(): void {
    this.eventSource?.close();
    this.eventSource = undefined;
  }

  /**
   * Emite una notificacion generada en el front (no viene del SSE). Se usa para mostrar
   * en el mismo toast las excepciones de dominio, ej. CU-13 sin mas sugerencias.
   */
  emitirLocal(notificacion: Notificacion): void {
    this.notificacionesSubject.next(notificacion);
  }

  /** CU-18 (gestionar solicitudes): solicitudes de amistad pendientes del usuario actual. */
  obtenerPendientes(): Observable<SolicitudAmistad[]> {
    const params = new HttpParams().set('usuarioId', this.usuarioActualId());
    return this.http.get<SolicitudAmistad[]>(`${API_URL}/solicitudes/pendientes`, { params });
  }

  aceptarSolicitud(token: string): Observable<unknown> {
    const params = new HttpParams().set('token', token);
    return this.http.post(`${API_URL}/solicitudes/aceptar`, {}, { params });
  }

  rechazarSolicitud(token: string): Observable<unknown> {
    const params = new HttpParams().set('token', token);
    return this.http.post(`${API_URL}/solicitudes/rechazar`, {}, { params });
  }

  /** Notificaciones no leidas (para el contador de la campana). */
  obtenerNoLeidas(): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(`${API_URL}/notificaciones/no-leidas`);
  }
}
