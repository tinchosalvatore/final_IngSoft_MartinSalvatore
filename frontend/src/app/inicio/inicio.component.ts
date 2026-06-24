import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NotificacionService } from '../services/notificacion.service';
import { SesionService } from '../services/sesion.service';
import { UsuarioService } from '../services/usuario.service';

/**
 * CU-2 Iniciar Sesion (= InicioUI del diagrama de secuencia). Valida email + contrasena contra
 * el backend (POST /usuarios/login). Al exito guarda la sesion, reabre el SSE como el usuario
 * logueado y navega al inicio. 401 = credenciales invalidas; 400 = cuenta bloqueada.
 */
@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './inicio.component.html',
  styleUrl: './inicio.component.css'
})
export class InicioComponent {

  email = '';
  contrasena = '';
  enviando = false;
  error = '';

  constructor(
    private usuarioService: UsuarioService,
    private sesion: SesionService,
    private notificacionService: NotificacionService,
    private router: Router
  ) {}

  iniciarSesion(): void {
    this.enviando = true;
    this.error = '';

    this.usuarioService.iniciarSesion(this.email.trim(), this.contrasena).subscribe({
      next: (usuario) => {
        this.sesion.setUsuario(usuario);
        this.notificacionService.reconectar();
        this.enviando = false;
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.error = err?.error?.mensaje ?? 'No se pudo iniciar sesion';
        this.enviando = false;
      }
    });
  }
}
