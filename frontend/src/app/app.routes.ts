import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { BuscadorComponent } from './buscador/buscador.component';
import { SolicitudesComponent } from './solicitudes/solicitudes.component';
import { ChatComponent } from './chat/chat.component';
import { PerfilComponent } from './perfil/perfil.component';

// Sin login/sesion (CU-1/CU-2 fuera de alcance): se entra directo como el usuario de la demo (martin).
export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'buscador', component: BuscadorComponent },
  { path: 'solicitudes', component: SolicitudesComponent },
  { path: 'chat/:id', component: ChatComponent },
  { path: 'perfil/:id', component: PerfilComponent },
  { path: '**', redirectTo: '' }
];
