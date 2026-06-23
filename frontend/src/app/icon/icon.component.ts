import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

/**
 * Iconos SVG de linea (estilo Feather), trazo en `currentColor` para que el color lo
 * defina el CSS del contenedor. Reemplaza a los emojis, que no renderizan en todos los
 * sistemas. Uso: <app-icon name="search" />.
 */
@Component({
  selector: 'app-icon',
  standalone: true,
  imports: [CommonModule],
  template: `
    <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
         [ngSwitch]="name" aria-hidden="true">

      <!-- lupa (buscar) -->
      <g *ngSwitchCase="'search'">
        <circle cx="11" cy="11" r="7" />
        <line x1="21" y1="21" x2="16.5" y2="16.5" />
      </g>

      <!-- campana (notificaciones) -->
      <g *ngSwitchCase="'bell'">
        <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.7 21a2 2 0 0 1-3.4 0" />
      </g>

      <!-- persona + (crear cuenta) -->
      <g *ngSwitchCase="'user-plus'">
        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
        <circle cx="9" cy="7" r="4" />
        <line x1="19" y1="8" x2="19" y2="14" />
        <line x1="22" y1="11" x2="16" y2="11" />
      </g>

      <!-- like (me gusta) -->
      <g *ngSwitchCase="'like'">
        <path d="M7 10v11H4a1 1 0 0 1-1-1v-9a1 1 0 0 1 1-1h3z" />
        <path d="M7 10l4-7a2 2 0 0 1 2 2v3h5a2 2 0 0 1 2 2.3l-1.3 7A2 2 0 0 1 18.7 20H7" />
      </g>

      <!-- comentario -->
      <g *ngSwitchCase="'comment'">
        <path d="M21 11.5a8 8 0 0 1-11.5 7L3 20l1.5-4.5A8 8 0 1 1 21 11.5z" />
      </g>

      <!-- compartir -->
      <g *ngSwitchCase="'share'">
        <circle cx="18" cy="5" r="3" />
        <circle cx="6" cy="12" r="3" />
        <circle cx="18" cy="19" r="3" />
        <line x1="8.6" y1="13.5" x2="15.4" y2="17.5" />
        <line x1="15.4" y1="6.5" x2="8.6" y2="10.5" />
      </g>

      <!-- torta (cumpleaños) -->
      <g *ngSwitchCase="'cake'">
        <path d="M4 21h16v-7a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v7z" />
        <path d="M4 16h16" />
        <line x1="8" y1="8" x2="8" y2="6" />
        <line x1="12" y1="8" x2="12" y2="6" />
        <line x1="16" y1="8" x2="16" y2="6" />
      </g>

      <!-- personas (solicitud de amistad) -->
      <g *ngSwitchCase="'people'">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
        <circle cx="9" cy="7" r="4" />
        <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
        <path d="M16 3.13a4 4 0 0 1 0 7.75" />
      </g>

    </svg>
  `
})
export class IconComponent {
  @Input() name = '';
  @Input() size = 20;
}
