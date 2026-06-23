import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { BuscadorComponent } from './buscador/buscador.component';
import { RegistroComponent } from './registro/registro.component';
import { SolicitudesComponent } from './solicitudes/solicitudes.component';
import { ChatComponent } from './chat/chat.component';
import { PerfilComponent } from './perfil/perfil.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'buscador', component: BuscadorComponent },
  { path: 'registro', component: RegistroComponent },
  { path: 'solicitudes', component: SolicitudesComponent },
  { path: 'chat/:id', component: ChatComponent },
  { path: 'perfil/:id', component: PerfilComponent },
  { path: '**', redirectTo: '' }
];
