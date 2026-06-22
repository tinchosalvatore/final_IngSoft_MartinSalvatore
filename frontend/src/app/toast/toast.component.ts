import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { Notificacion } from '../models/notificacion';
import { NotificacionService } from '../services/notificacion.service';

interface ToastVisible extends Notificacion {
  uid: number;
}

/**
 * Barra de toasts global. Se monta una sola vez (en el AppComponent), abre la
 * conexion SSE y muestra cada notificacion entrante (CU-14 y CU-15) en vivo.
 * Click en el toast -> navega segun el tipo.
 */
@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.css'
})
export class ToastComponent implements OnInit, OnDestroy {

  toasts: ToastVisible[] = [];
  private sub?: Subscription;
  private contador = 0;

  constructor(private notificacionService: NotificacionService, private router: Router) {}

  ngOnInit(): void {
    this.notificacionService.conectar();
    this.sub = this.notificacionService.notificaciones$.subscribe((n) => this.mostrar(n));
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  private mostrar(n: Notificacion): void {
    const toast: ToastVisible = { ...n, uid: ++this.contador };
    this.toasts = [toast, ...this.toasts];
    setTimeout(() => this.cerrar(toast.uid), 6000);
  }

  cerrar(uid: number): void {
    this.toasts = this.toasts.filter((t) => t.uid !== uid);
  }

  icono(tipo: string): string {
    return tipo === 'CUMPLEANOS' ? '\u{1F382}' : '\u{1F465}';
  }

  alClickear(toast: ToastVisible): void {
    if (toast.tipo === 'SOLICITUD_AMISTAD') {
      this.router.navigate(['/solicitudes']);
    } else if (toast.tipo === 'CUMPLEANOS') {
      // CU-15: click en la notificacion -> chat con el cumpleañero.
      const nombre = toast.mensaje.replace(/ cumple años hoy.*$/i, '').trim();
      this.router.navigate(['/chat', toast.referenciaId], { queryParams: { nombre, cumple: 1 } });
    }
    this.cerrar(toast.uid);
  }
}
