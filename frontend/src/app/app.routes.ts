import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { BuscadorComponent } from './buscador/buscador.component';
import { RegistroComponent } from './registro/registro.component';
import { InicioComponent } from './inicio/inicio.component';
import { SolicitudesComponent } from './solicitudes/solicitudes.component';
import { ChatComponent } from './chat/chat.component';
import { PerfilComponent } from './perfil/perfil.component';
import { sesionGuard } from './guards/sesion.guard';

export const routes: Routes = [
  // CU-2 (login) y CU-1 (registro): publicas, no requieren sesion.
  { path: 'login', component: InicioComponent },
  { path: 'registro', component: RegistroComponent },
  // Pantallas que asumen un usuario logueado (CU-13/14/15/7/18): protegidas por el guard.
  { path: '', component: HomeComponent, canActivate: [sesionGuard] },
  { path: 'buscador', component: BuscadorComponent, canActivate: [sesionGuard] },
  { path: 'solicitudes', component: SolicitudesComponent, canActivate: [sesionGuard] },
  { path: 'chat/:id', component: ChatComponent, canActivate: [sesionGuard] },
  { path: 'perfil/:id', component: PerfilComponent, canActivate: [sesionGuard] },
  { path: '**', redirectTo: '' }
];
