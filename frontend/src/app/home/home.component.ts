import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

interface Publicacion {
  autor: string;
  iniciales: string;
  tiempo: string;
  texto: string;
  meGusta: number;
  comentarios: number;
}

/**
 * Home de UM-Book. SOLO UI (datos mockeados). El unico elemento real es el link
 * al buscador (CU-13) y el alta de usuario. La campana de notificaciones se conecta
 * al stream SSE en el objetivo 6.
 */
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

  usuarioActual = { nombre: 'Martin', iniciales: 'MS' };

  accesos = ['Amigos', 'Grupos', 'Marketplace', 'Albumes', 'Eventos'];

  contactos = ['Ana Gomez', 'Beto Diaz', 'Carla Lopez', 'Diego Ruiz', 'Eva Mura'];

  cumpleanos = ['Ana Gomez cumple hoy'];

  publicaciones: Publicacion[] = [
    {
      autor: 'Ana Gomez', iniciales: 'AG', tiempo: 'hace 2 h',
      texto: 'Arranco el cuatrimestre con todo. Quien se prende a estudiar Ing. de Software?',
      meGusta: 12, comentarios: 4
    },
    {
      autor: 'Beto Diaz', iniciales: 'BD', tiempo: 'hace 5 h',
      texto: 'Subi las fotos del asado de la facu al album. Pasen a verlas!',
      meGusta: 28, comentarios: 9
    },
    {
      autor: 'Carla Lopez', iniciales: 'CL', tiempo: 'ayer',
      texto: 'Alguien tiene los apuntes de Proceso Unificado? Gracias UM-Book <3',
      meGusta: 7, comentarios: 15
    }
  ];
}
