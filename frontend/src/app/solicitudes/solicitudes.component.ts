import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SolicitudAmistad } from '../models/notificacion';
import { NotificacionService } from '../services/notificacion.service';

/**
 * CU-14 (click en la notificacion): pantalla de solicitudes de amistad pendientes,
 * con acciones de aceptar / rechazar.
 */
@Component({
  selector: 'app-solicitudes',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './solicitudes.component.html',
  styleUrl: './solicitudes.component.css'
})
export class SolicitudesComponent implements OnInit {

  solicitudes: SolicitudAmistad[] = [];
  cargando = true;
  aviso = '';

  constructor(private notificacionService: NotificacionService) {}

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.notificacionService.obtenerPendientes().subscribe({
      next: (data) => {
        this.solicitudes = data;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
      }
    });
  }

  aceptar(s: SolicitudAmistad): void {
    this.notificacionService.aceptarSolicitud(s.tokenEmail).subscribe(() => {
      this.quitar(s);
      this.aviso = `Ahora sos amigo de ${s.remitenteNombre}`;
    });
  }

  rechazar(s: SolicitudAmistad): void {
    this.notificacionService.rechazarSolicitud(s.tokenEmail).subscribe(() => {
      this.quitar(s);
      this.aviso = `Rechazaste la solicitud de ${s.remitenteNombre}`;
    });
  }

  private quitar(s: SolicitudAmistad): void {
    this.solicitudes = this.solicitudes.filter((x) => x.id !== s.id);
  }
}
