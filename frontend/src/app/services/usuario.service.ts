import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import { Usuario } from '../models/usuario';
import { SesionService } from './sesion.service';

/**
 * Cliente del endpoint de usuarios. CU-1 (registro), CU-2 (login), CU-7 (buscar),
 * CU-13 (amigos en comun). El "usuario actual" lo aporta la sesion (CU-2): se manda como
 * usuarioId en cada consulta.
 */
@Injectable({ providedIn: 'root' })
export class UsuarioService {

  constructor(private http: HttpClient, private sesion: SesionService) {}

  /** Agrega usuarioId (el logueado) a los params, si hay sesion. */
  private conUsuario(params: HttpParams = new HttpParams()): HttpParams {
    const id = this.sesion.usuarioId;
    return id != null ? params.set('usuarioId', id) : params;
  }

  /** CU-2: inicia sesion. POST /usuarios/login. */
  iniciarSesion(email: string, contrasena: string): Observable<Usuario> {
    return this.http.post<Usuario>(`${API_URL}/usuarios/login`, { email, contrasena });
  }

  /** CU-13: usuarios con al menos {@code minAmigos} amigos en comun con el usuario de referencia. */
  buscarConAmigosEnComun(minAmigos: number): Observable<Usuario[]> {
    const params = this.conUsuario(new HttpParams().set('amigosEnComun', minAmigos));
    return this.http.get<Usuario[]>(`${API_URL}/usuarios`, { params });
  }

  /** CU-7: busqueda por texto de la searchbar: GET /usuarios/buscar?nombre=&apellido=. */
  buscarPorTexto(texto: string): Observable<Usuario[]> {
    const params = this.conUsuario(new HttpParams().set('nombre', texto).set('apellido', texto));
    return this.http.get<Usuario[]>(`${API_URL}/usuarios/buscar`, { params });
  }

  /** CU-15: usuarios que cumplen años hoy. Alimenta la tarjeta de Cumpleaños del home. */
  cumpleanosDeHoy(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${API_URL}/usuarios/cumpleanos`, { params: this.conUsuario() });
  }

  /**
   * Demo del boton "recargar sugerencias": POST /usuarios/sugerencia-extra.
   * Agrega un lote de dos usuarios con +2 amigos en comun; 404 cuando no quedan mas.
   */
  agregarSugerenciaExtra(): Observable<Usuario[]> {
    return this.http.post<Usuario[]>(`${API_URL}/usuarios/sugerencia-extra`, {}, { params: this.conUsuario() });
  }

  /** CU-1: alta de usuario: POST /usuarios. */
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
