import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import { Usuario } from '../models/usuario';

/**
 * Cliente del endpoint de usuarios. CU-13: GET /usuarios?amigosEnComun=N.
 */
@Injectable({ providedIn: 'root' })
export class UsuarioService {

  constructor(private http: HttpClient) {}

  /** Lista usuarios con al menos {@code minAmigos} amigos en comun con el usuario de referencia. */
  buscarConAmigosEnComun(minAmigos: number): Observable<Usuario[]> {
    const params = new HttpParams().set('amigosEnComun', minAmigos);
    return this.http.get<Usuario[]>(`${API_URL}/usuarios`, { params });
  }

  /** Busqueda por texto de la searchbar: GET /usuarios?q=texto. */
  buscarPorTexto(texto: string): Observable<Usuario[]> {
    const params = new HttpParams().set('q', texto);
    return this.http.get<Usuario[]>(`${API_URL}/usuarios`, { params });
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

  /** Alta de usuario: POST /usuarios. */
  registrar(datos: RegistroDatos): Observable<Usuario> {
    return this.http.post<Usuario>(`${API_URL}/usuarios`, datos);
  }
}

/** Datos de entrada para el alta (espeja RegistroDTO.java). */
export interface RegistroDatos {
  nombre: string;
  apellido: string;
  email: string;
  nombreUsuario: string;
  contrasena: string;
  fechaNacimiento: string;
}
