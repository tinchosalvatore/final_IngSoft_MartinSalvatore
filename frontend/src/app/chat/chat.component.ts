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

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.amigo = this.route.snapshot.queryParamMap.get('nombre') ?? 'tu amigo';
    this.iniciales = this.amigo
      .split(' ')
      .map((p) => p.charAt(0))
      .join('')
      .slice(0, 2)
      .toUpperCase();

    const esCumple = this.route.snapshot.queryParamMap.get('cumple') === '1';
    this.mensajes = [
      { de: 'otro', texto: `Hola! Soy ${this.amigo}` },
      { de: 'otro', texto: esCumple ? 'Gracias por acordarte de mi cumple! 🎂' : 'Como va todo?' }
    ];
    if (esCumple) {
      this.borrador = `Feliz cumple, ${this.amigo}! 🎉`;
    }
  }

  enviar(): void {
    const texto = this.borrador.trim();
    if (!texto) {
      return;
    }
    this.mensajes = [...this.mensajes, { de: 'yo', texto }];
    this.borrador = '';
  }
}
