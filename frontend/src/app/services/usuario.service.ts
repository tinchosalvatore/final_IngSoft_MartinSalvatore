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
}
