import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { IconComponent } from '../icon/icon.component';
import { Notificacion } from '../models/notificacion';
import { Usuario } from '../models/usuario';
import { NotificacionService } from '../services/notificacion.service';
import { UsuarioService } from '../services/usuario.service';

/**
 * Perfil de usuario (SOLO UI, placeholder). Destino del click en una tarjeta del buscador.
 * Todo el contenido es fijo salvo el nombre, el icono (iniciales) y los amigos en comun,
 * que llegan por queryParams desde el buscador.
 */
@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, RouterLink, IconComponent],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.css'
})
export class PerfilComponent implements OnInit {

  /** Minimo de amigos en comun, igual que el buscador (CU-13): fijo en 2. */
  private static readonly MIN_AMIGOS = 2;

  nombre = 'Usuario';
  usuario = 'usuario';
  iniciales = '?';
  amigosEnComun = 0;

  /** Sugeridos con +2 amigos en comun, misma data que el buscador (tira horizontal). */
  sugeridos: Usuario[] = [];
  /** true mientras se procesa la recarga (deshabilita el boton). */
  recargando = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private usuarioService: UsuarioService,
    private notificacionService: NotificacionService
  ) {}

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    this.nombre = params.get('nombre') ?? 'Usuario';
    this.usuario = params.get('usuario') ?? 'usuario';
    this.amigosEnComun = Number(params.get('amigos') ?? 0);
    this.iniciales = this.nombre
      .split(' ')
      .map((p) => p.charAt(0))
      .join('')
      .slice(0, 2)
      .toUpperCase();

    this.cargarSugeridos();
  }

  /** Mismas sugerencias que el buscador (CU-13): usuarios con +2 amigos en comun. */
  private cargarSugeridos(): void {
    this.usuarioService
      .buscarConAmigosEnComun(PerfilComponent.MIN_AMIGOS)
      .pipe(catchError(() => of([] as Usuario[])))
      .subscribe((data) => (this.sugeridos = data));
  }

  /**
   * Boton recargar: pide al backend mas usuarios con +2 amigos en comun y refresca la tira.
   * Cuando ya no quedan, el backend responde 404 y se muestra la notificacion de excepcion.
   */
  recargarSugerencias(): void {
    if (this.recargando) {
      return;
    }
    this.recargando = true;
    this.usuarioService.agregarSugerenciaExtra().subscribe({
      next: () => {
        this.recargando = false;
        this.cargarSugeridos();
      },
      error: (err) => {
        this.recargando = false;
        const mensaje = err?.error?.mensaje ?? 'No hay mas usuarios sugeridos';
        this.notificarExcepcion(mensaje);
      }
    });
  }

  /** Empuja la excepcion al toast global, reusando el mecanismo de notificaciones. */
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

  /** Iniciales para el avatar de cada sugerido. */
  inicialesDe(u: Usuario): string {
    return (u.nombre.charAt(0) + u.apellido.charAt(0)).toUpperCase();
  }

  /** Click en un sugerido -> abre su perfil (igual que el buscador). */
  verPerfil(u: Usuario): void {
    this.router.navigate(['/perfil', u.id], {
      queryParams: {
        nombre: `${u.nombre} ${u.apellido}`,
        usuario: u.nombreUsuario,
        amigos: u.amigosEnComun
      }
    });
  }
}
