import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SesionService } from '../services/sesion.service';

/**
 * Guard de sesion (CU-2): si no hay usuario logueado, redirige a /login. Protege las pantallas
 * que asumen un "usuario actual" (home, buscador, solicitudes, perfil, chat).
 */
export const sesionGuard: CanActivateFn = () => {
  const sesion = inject(SesionService);
  const router = inject(Router);
  if (sesion.estaLogueado()) {
    return true;
  }
  return router.createUrlTree(['/login']);
};
