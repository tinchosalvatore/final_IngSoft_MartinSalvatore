/** DTO de usuario devuelto por el backend (espeja UsuarioDTO.java). */
export interface Usuario {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  nombreUsuario: string;
  fechaNacimiento: string;
  amigosEnComun: number;
}
