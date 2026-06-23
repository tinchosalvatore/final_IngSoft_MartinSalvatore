import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

interface Mensaje {
  de: 'yo' | 'otro';
  texto: string;
}

/**
 * Chat (SOLO UI, datos mockeados). Es el destino del click en la notificacion de
 * cumpleaños (CU-15: "mostrarPantallaChat"). No persiste: el envio hace eco local.
 */
@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit {

  amigo = 'tu amigo';
  iniciales = '?';
  borrador = '';
  mensajes: Mensaje[] = [];

  private esCumple = false;
  /** true una vez que el amigo agradecio (asi no repite la respuesta de cumple). */
  private agradecio = false;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.amigo = this.route.snapshot.queryParamMap.get('nombre') ?? 'tu amigo';
    this.iniciales = this.amigo
      .split(' ')
      .map((p) => p.charAt(0))
      .join('')
      .slice(0, 2)
      .toUpperCase();

    this.esCumple = this.route.snapshot.queryParamMap.get('cumple') === '1';
    this.mensajes = [
      { de: 'otro', texto: `Hola! Soy ${this.amigo}` },
      { de: 'otro', texto: 'Como va todo?' }
    ];
    if (this.esCumple) {
      this.borrador = `Feliz cumple, ${this.amigo}!`;
    }
  }

  enviar(): void {
    const texto = this.borrador.trim();
    if (!texto) {
      return;
    }
    this.mensajes = [...this.mensajes, { de: 'yo', texto }];
    this.borrador = '';

    // Demo de cumple: el amigo agradece recien despues de enviar el saludo.
    if (this.esCumple && !this.agradecio) {
      this.agradecio = true;
      setTimeout(() => {
        this.mensajes = [...this.mensajes, { de: 'otro', texto: 'Gracias por acordarte de mi cumple!' }];
      }, 800);
    }
  }
}
