import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import { Usuario } from '../models/usuario';

/**
 * Cliente del endpoint de usuarios. CU-7 (buscar), CU-13 (amigos en comun), CU-15 (cumpleaños).
 * No hay login/sesion: el "usuario actual" lo resuelve el backend (martin, id=1), por eso no se
 * manda usuarioId en las consultas.
 */
@Injectable({ providedIn: 'root' })
export class UsuarioService {

  constructor(private http: HttpClient) {}

  /** CU-13: usuarios con al menos {@code minAmigos} amigos en comun con el usuario de referencia. */
  buscarConAmigosEnComun(minAmigos: number): Observable<Usuario[]> {
    const params = new HttpParams().set('amigosEnComun', minAmigos);
    return this.http.get<Usuario[]>(`${API_URL}/usuarios`, { params });
  }

  /** CU-7: busqueda por texto de la searchbar: GET /usuarios/buscar?nombre=&apellido=. */
  buscarPorTexto(texto: string): Observable<Usuario[]> {
    const params = new HttpParams().set('nombre', texto).set('apellido', texto);
    return this.http.get<Usuario[]>(`${API_URL}/usuarios/buscar`, { params });
  }

  /** CU-15: usuarios que cumplen años hoy. Alimenta la tarjeta de Cumpleaños del home. */
  cumpleanosDeHoy(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${API_URL}/usuarios/cumpleanos`);
  }

  /**
   * Demo del boton "recargar sugerencias": POST /usuarios/sugerencia-extra.
   * Agrega un lote de dos usuarios con +2 amigos en comun; 404 cuando no quedan mas.
   */
  agregarSugerenciaExtra(): Observable<Usuario[]> {
    return this.http.post<Usuario[]>(`${API_URL}/usuarios/sugerencia-extra`, {});
  }
}
