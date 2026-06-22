import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { RegistroDatos, UsuarioService } from '../services/usuario.service';

/**
 * Alta simple de usuario (con cumpleaños). POST /usuarios.
 */
@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css'
})
export class RegistroComponent {

  datos: RegistroDatos = {
    nombre: '',
    apellido: '',
    email: '',
    nombreUsuario: '',
    contrasena: '',
    fechaNacimiento: ''
  };

  enviando = false;
  exito = '';
  error = '';

  constructor(private usuarioService: UsuarioService) {}

  registrar(): void {
    this.enviando = true;
    this.exito = '';
    this.error = '';

    this.usuarioService.registrar(this.datos).subscribe({
      next: (u) => {
        this.exito = `Usuario @${u.nombreUsuario} registrado con exito`;
        this.enviando = false;
        this.datos = { nombre: '', apellido: '', email: '', nombreUsuario: '', contrasena: '', fechaNacimiento: '' };
      },
      error: (err) => {
        this.error = err?.error?.mensaje ?? 'No se pudo registrar el usuario';
        this.enviando = false;
      }
    });
  }
}
