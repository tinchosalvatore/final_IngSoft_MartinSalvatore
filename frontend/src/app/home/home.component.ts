import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { IconComponent } from '../icon/icon.component';
import { Usuario } from '../models/usuario';
import { UsuarioService } from '../services/usuario.service';

interface Publicacion {
  autor: string;
  iniciales: string;
  tiempo: string;
  texto: string;
  meGusta: number;
  comentarios: number;
  /** Datos para abrir el perfil (igual que el buscador). */
  perfil: PerfilLink;
}

/** Minimo para navegar a un perfil placeholder, espeja los queryParams del buscador. */
interface PerfilLink {
  id: number;
  nombre: string;
  usuario: string;
  amigos: number;
}

/**
 * Home de UM-Book. Feed mockeado, pero las tarjetas de usuario abren su perfil (igual
 * que el buscador) y la tarjeta de Cumpleaños es real: se llena con los usuarios que
 * cumplen años hoy (CU-15) desde el backend.
 */
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, IconComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {

  /** Usuario actual de la demo (martin), para el encabezado y sus propias publicaciones. */
  usuarioActual = { nombre: 'Martin', iniciales: 'M' };

  /** Perfil del usuario actual, para enlazar sus propias publicaciones. */
  private perfilActual: PerfilLink = { id: 1, nombre: 'Martin Salvatore', usuario: 'martin', amigos: 0 };

  /** Texto del cuadro "Que estas pensando". */
  borrador = '';

  accesos = ['Amigos', 'Grupos', 'Marketplace', 'Albumes', 'Eventos'];

  contactos: PerfilLink[] = [
    { id: 2, nombre: 'Ana Gomez', usuario: 'ana', amigos: 0 },
    { id: 3, nombre: 'Beto Diaz', usuario: 'beto', amigos: 0 },
    { id: 4, nombre: 'Carla Lopez', usuario: 'carla', amigos: 0 },
    { id: 5, nombre: 'Diego Ruiz', usuario: 'diego', amigos: 2 },
    { id: 6, nombre: 'Eva Mura', usuario: 'eva', amigos: 3 }
  ];

  /** Cumpleañeros de hoy, traidos del backend (CU-15). */
  cumpleanos: Usuario[] = [];

  publicaciones: Publicacion[] = [
    {
      autor: 'Ana Gomez', iniciales: 'AG', tiempo: 'hace 2 h',
      texto: 'Arranco el cuatrimestre con todo. Quien se prende a estudiar Ing. de Software?',
      meGusta: 12, comentarios: 4,
      perfil: { id: 2, nombre: 'Ana Gomez', usuario: 'ana', amigos: 0 }
    },
    {
      autor: 'Beto Diaz', iniciales: 'BD', tiempo: 'hace 5 h',
      texto: 'Subi las fotos del asado de la facu al album. Pasen a verlas!',
      meGusta: 28, comentarios: 9,
      perfil: { id: 3, nombre: 'Beto Diaz', usuario: 'beto', amigos: 0 }
    },
    {
      autor: 'Carla Lopez', iniciales: 'CL', tiempo: 'ayer',
      texto: 'Alguien tiene los apuntes de Proceso Unificado? Gracias UM-Book <3',
      meGusta: 7, comentarios: 15,
      perfil: { id: 4, nombre: 'Carla Lopez', usuario: 'carla', amigos: 0 }
    }
  ];

  constructor(
    private usuarioService: UsuarioService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.usuarioService.cumpleanosDeHoy().subscribe({
      next: (data) => (this.cumpleanos = data),
      error: () => (this.cumpleanos = [])
    });
  }

  /**
   * Publica lo escrito en el cuadro. Solo UI / no persistente: agrega la publicacion arriba
   * del feed en memoria. Al refrescar la pagina se pierde.
   */
  publicar(): void {
    const texto = this.borrador.trim();
    if (!texto) {
      return;
    }
    this.publicaciones = [
      {
        autor: this.usuarioActual.nombre,
        iniciales: this.usuarioActual.iniciales,
        tiempo: 'ahora',
        texto,
        meGusta: 0,
        comentarios: 0,
        perfil: this.perfilActual
      },
      ...this.publicaciones
    ];
    this.borrador = '';
  }

  /** Abre el perfil placeholder del usuario, con los mismos queryParams que el buscador. */
  verPerfil(p: PerfilLink): void {
    this.router.navigate(['/perfil', p.id], {
      queryParams: { nombre: p.nombre, usuario: p.usuario, amigos: p.amigos }
    });
  }

  /** Lo mismo pero a partir del DTO de cumpleaños. */
  verPerfilCumple(u: Usuario): void {
    this.verPerfil({
      id: u.id,
      nombre: `${u.nombre} ${u.apellido}`,
      usuario: u.nombreUsuario,
      amigos: u.amigosEnComun
    });
  }
}
