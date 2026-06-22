import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Usuario } from '../models/usuario';
import { UsuarioService } from '../services/usuario.service';

/**
 * CU-13: Buscador de usuarios con +2 amigos en comun.
 * Llama a GET /usuarios?amigosEnComun=N y muestra la lista; maneja el caso vacio (404).
 */
@Component({
  selector: 'app-buscador',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './buscador.component.html',
  styleUrl: './buscador.component.css'
})
export class BuscadorComponent {

  minAmigos = 2;
  usuarios: Usuario[] = [];
  mensaje = '';
  buscando = false;
  yaBusco = false;

  constructor(private usuarioService: UsuarioService) {}

  buscar(): void {
    this.buscando = true;
    this.yaBusco = true;
    this.mensaje = '';
    this.usuarios = [];

    this.usuarioService.buscarConAmigosEnComun(this.minAmigos).subscribe({
      next: (data) => {
        this.usuarios = data;
        this.buscando = false;
      },
      error: (err) => {
        // El backend responde 404 con { mensaje } cuando no hay resultados (CU-13 alt vacia).
        this.mensaje = err?.error?.mensaje ?? 'Ocurrio un error al buscar usuarios';
        this.buscando = false;
      }
    });
  }
}
