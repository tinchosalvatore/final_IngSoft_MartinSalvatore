import { Routes } from '@angular/router';
import { BuscadorComponent } from './buscador/buscador.component';

export const routes: Routes = [
  // El Home real (solo UI) se construye en el objetivo 5. Por ahora la raiz lleva al buscador.
  { path: '', redirectTo: 'buscador', pathMatch: 'full' },
  { path: 'buscador', component: BuscadorComponent },
  { path: '**', redirectTo: 'buscador' }
];
