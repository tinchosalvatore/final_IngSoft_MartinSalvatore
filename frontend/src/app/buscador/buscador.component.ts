import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { IconComponent } from '../icon/icon.component';
import { Notificacion } from '../models/notificacion';
import { Usuario } from '../models/usuario';
import { NotificacionService } from '../services/notificacion.service';
import { UsuarioService } from '../services/usuario.service';

/**
 * Buscador de usuarios, estilo red social.
 *  - Sin texto: muestra sugerencias = CU-13, usuarios con +2 amigos en comun (fijo).
 *  - Con texto: busca los usuarios creados por nombre/apellido (GET /usuarios?q=...),
 *    con debounce para no pegarle al backend en cada tecla.
 */
@Component({
  selector: 'app-buscador',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, IconComponent],
  templateUrl: './buscador.component.html',
  styleUrl: './buscador.component.css'
})
export class BuscadorComponent implements OnInit, OnDestroy {

  /** Minimo de amigos en comun para las sugerencias. Es el CU-13: fijo en 2. */
  private static readonly MIN_AMIGOS = 2;

  query = '';
  usuarios: Usuario[] = [];
  buscando = false;
  /** true mientras hay texto escrito (cambia el titulo/mensaje vacio). */
  modoBusqueda = false;
  /** true mientras se procesa la recarga de sugerencias (deshabilita el boton). */
  recargando = false;

  private termino$ = new Subject<string>();
  private sub?: Subscription;

  constructor(
    private usuarioService: UsuarioService,
    private notificacionService: NotificacionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Pipeline reactivo: espera 300ms tras la ultima tecla, ignora repetidos, y
    // segun haya texto o no, busca por nombre o trae las sugerencias del CU-13.
    this.sub = this.termino$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((texto) => {
          this.buscando = true;
          this.modoBusqueda = texto.trim().length > 0;
          const peticion = this.modoBusqueda
            ? this.usuarioService.buscarPorTexto(texto.trim())
            : this.usuarioService.buscarConAmigosEnComun(BuscadorComponent.MIN_AMIGOS);
          // El backend devuelve 404 cuando no hay sugerencias: lo tratamos como lista vacia.
          return peticion.pipe(catchError(() => of([] as Usuario[])));
        })
      )
      .subscribe((data) => {
        this.usuarios = data;
        this.buscando = false;
      });

    // Carga inicial: dispara las sugerencias (texto vacio).
    this.termino$.next('');
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  /** Cada tecla en la searchbar empuja el termino al pipeline. */
  alEscribir(): void {
    this.termino$.next(this.query);
  }

  /** Click en una tarjeta -> abre el perfil (placeholder) del usuario. */
  verPerfil(u: Usuario): void {
    this.router.navigate(['/perfil', u.id], {
      queryParams: {
        nombre: `${u.nombre} ${u.apellido}`,
        usuario: u.nombreUsuario,
        amigos: u.amigosEnComun
      }
    });
  }

  /**
   * Boton "recargar sugerencias" (CU-13): pide al backend un usuario mas con +2 amigos en
   * comun y recarga la lista. Cuando ya no quedan, el backend responde 404 y se muestra la
   * notificacion de excepcion ("no hay mas") en el toast global.
   */
  recargarSugerencias(): void {
    if (this.recargando) {
      return;
    }
    this.recargando = true;
    this.usuarioService.agregarSugerenciaExtra().subscribe({
      next: (lote) => {
        this.recargando = false;
        // Muestra solo el lote nuevo: deja de mostrar las dos sugerencias previas.
        this.modoBusqueda = false;
        this.query = '';
        this.usuarios = lote;
      },
      error: (err) => {
        this.recargando = false;
        const mensaje = err?.error?.mensaje ?? 'No hay mas usuarios sugeridos';
        this.notificarExcepcion(mensaje);
      }
    });
  }

  /** Empuja la excepcion al toast global, reusando el mismo mecanismo de notificaciones. */
  private notificarExcepcion(mensaje: string): void {
    const noti: Notificacion = {
      id: -1,
      tipo: 'ERROR',
      mensaje,
      referenciaId: -1,
      leida: false,
      fechaCreacion: new Date().toISOString()
    };
    this.notificacionService.emitirLocal(noti);
  }
}
