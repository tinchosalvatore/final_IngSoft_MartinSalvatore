#!/usr/bin/env bash
# boot.sh — levanta UM-Book completo (backend Spring Boot + frontend Angular).
# Uso: ./boot.sh            -> backend con H2 (la demo arranca sola)
#      ./boot.sh mysql      -> backend con perfil MySQL 8.4
# Cortar con Ctrl+C: frena ambos procesos de una.

set -e

# Raíz del repo (donde está este script), sin importar desde dónde se llame.
RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Java 21 es obligatorio por la spec (tenemos varias versiones instaladas vía sdkman).
export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.11-tem"
export PATH="$JAVA_HOME/bin:$PATH"

# Perfil de DB: H2 por defecto, o 'mysql' si se pasa como primer argumento.
PERFIL="${1:-}"

# Evita el prompt interactivo de analytics de Angular (se cuelga al correr en background).
export NG_CLI_ANALYTICS=false

# Al salir (Ctrl+C o fin), matar backend y frontend juntos para no dejar puertos colgados.
limpiar() {
  echo ""
  echo "[boot] cerrando procesos..."
  kill 0 2>/dev/null || true
}
trap limpiar EXIT INT TERM

# --- Backend: Spring Boot en :8080 ---
echo "[boot] arrancando backend (Spring Boot, Java 21) en http://localhost:8080 ..."
if [ -n "$PERFIL" ]; then
  echo "[boot] perfil de DB: $PERFIL"
  ( cd "$RAIZ/backend" && ./mvnw spring-boot:run -Dspring-boot.run.profiles="$PERFIL" ) &
else
  echo "[boot] perfil de DB: H2 (default)"
  ( cd "$RAIZ/backend" && ./mvnw spring-boot:run ) &
fi

# --- Frontend: Angular dev server en :4200 ---
# Instalar dependencias solo si falta node_modules (primera corrida).
if [ ! -d "$RAIZ/frontend/node_modules" ]; then
  echo "[boot] node_modules ausente: instalando dependencias del frontend..."
  ( cd "$RAIZ/frontend" && npm install )
fi

echo "[boot] arrancando frontend (Angular) en http://localhost:4200 ..."
( cd "$RAIZ/frontend" && npm start ) &

# Abrir el navegador en el front cuando :4200 ya responda (espera el primer build de Angular).
URL_FRONT="http://localhost:4200"
(
  for _ in $(seq 1 60); do
    if curl -sf -o /dev/null "$URL_FRONT"; then
      echo "[boot] front listo: abriendo navegador en $URL_FRONT ..."
      xdg-open "$URL_FRONT" >/dev/null 2>&1 || echo "[boot] abri manualmente: $URL_FRONT"
      break
    fi
    sleep 2
  done
) &

# Esperar a que cualquiera de los dos termine; el trap limpia el resto.
echo "[boot] todo levantado. Ctrl+C para frenar."
wait
