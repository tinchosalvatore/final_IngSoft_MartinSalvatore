import { Injectable } from '@angular/core';
import { Usuario } from '../models/usuario';

const STORAGE_KEY = 'umbook.usuario';

/**
 * Sesion del usuario actual (CU-2). Guarda el usuario logueado y lo persiste en localStorage
 * para sobrevivir refresh. Reemplaza el viejo "usuario actual" hardcodeado (id=1): ahora el
 * id sale del login y se propaga al resto de la app (SSE, busquedas, solicitudes).
 */
@Injectable({ providedIn: 'root' })
export class SesionService {

  private usuario: Usuario | null = null;

  constructor() {
    const guardado = localStorage.getItem(STORAGE_KEY);
    if (guardado) {
      try {
        this.usuario = JSON.parse(guardado);
      } catch {
        this.usuario = null;
      }
    }
  }

  setUsuario(usuario: Usuario): void {
    this.usuario = usuario;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(usuario));
  }

  cerrarSesion(): void {
    this.usuario = null;
    localStorage.removeItem(STORAGE_KEY);
  }

  get usuarioActual(): Usuario | null {
    return this.usuario;
  }

  /** Id del usuario logueado, o null si no hay sesion. */
  get usuarioId(): number | null {
    return this.usuario ? this.usuario.id : null;
  }

  estaLogueado(): boolean {
    return this.usuario !== null;
  }
}
