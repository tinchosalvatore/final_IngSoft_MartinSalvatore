import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SolicitudAmistad } from '../models/notificacion';
import { NotificacionService } from '../services/notificacion.service';

/**
 * CU-14 (click en la notificacion): pantalla de solicitudes de amistad pendientes.
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

  constructor(private notificacionService: NotificacionService) {}

  ngOnInit(): void {
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
}
