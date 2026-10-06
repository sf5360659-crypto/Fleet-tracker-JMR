# Fleet Tracker — JMR Martinez Trucking

Sistema de rastreo GPS propio: app Android para el chofer + panel web con mapa en vivo.

## Cómo funciona

```
Teléfono del chofer (app Android)
   │  GPS cada 15 segundos
   ▼
Firebase Realtime Database (nube, gratis para empezar)
   │  se actualiza solo
   ▼
Panel web (mapa en vivo desde cualquier navegador)
```

- La app corre como **servicio en primer plano** con notificación visible: el chofer la inicia y la detiene. Nada oculto.
- Cada camión se identifica con un ID (ej. `truck-01`).
- El panel web muestra todos los camiones en un mapa, en tiempo real.

## Lo que necesitas (cuentas gratis)

1. **Cuenta de Google** → crea un proyecto en [Firebase Console](https://console.firebase.google.com).
2. En el proyecto, activa **Realtime Database** (modo de prueba para empezar).
3. Registra la app Android con el paquete `com.jmrmartinez.fleettracker` y descarga `google-services.json` → colócalo en `android-app/app/`.
4. En el panel web (`web-dashboard/index.html`), pega tu configuración de Firebase donde dice `firebaseConfig`.

## Cómo obtener el APK (compilar)

**Opción A — GitHub Actions (recomendada, gratis, desde el teléfono):**
1. Crea una cuenta gratis en github.com y un repositorio nuevo.
2. Sube todo el contenido de esta carpeta al repositorio.
3. En el repositorio: Settings → Secrets → Actions → crea el secreto
   `GOOGLE_SERVICES_JSON` y pega adentro el contenido completo de tu
   `google-services.json` de Firebase.
4. Ve a Actions → "Build APK" → Run workflow.
5. Al terminar, descarga el APK desde "Artifacts" e instálalo en tu teléfono
   (permite "instalar apps desconocidas" cuando Android lo pida).

**Opción B — En una computadora con Android Studio:**
1. Instala Android Studio, abre la carpeta `android-app`.
2. Coloca tu `google-services.json` real en `android-app/app/`
   (reemplaza el de prueba que viene incluido).
3. Menú Build → Build APK. El archivo sale en
   `app/build/outputs/apk/debug/app-debug.apk`.

## Estructura

```
fleet-tracker/
├── android-app/            # App del chofer (Kotlin)
│   ├── app/
│   │   ├── build.gradle
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/jmrmartinez/fleettracker/
│   │       │   ├── MainActivity.kt      # Pantalla: ID del camión + iniciar/detener
│   │       │   └── TrackingService.kt   # Servicio que manda el GPS a Firebase
│   │       └── res/layout/activity_main.xml
│   └── build.gradle / settings.gradle
├── web-dashboard/
│   └── index.html          # Mapa en vivo (Leaflet + Firebase)
└── README.md
```

## Datos guardados (por camión)

`companies/jmr-martinez/trucks/{truckId}/live`:
```json
{
  "lat": 32.7767,
  "lng": -96.7970,
  "speed": 28.5,
  "accuracy": 8.0,
  "driver": "Sergio",
  "updatedAt": 1728000000000
}
```

## Notas honestas

- **No es un ELD certificado.** Para las horas de servicio ante la FMCSA sigues necesitando un ELD avalado. Esto es rastreo operativo, no cumplimiento regulatorio.
- **Batería:** el GPS constante gasta pila. El teléfono debe ir conectado al cargador del camión.
- **Permisos:** en Android 10+ la app pide ubicación "permitir siempre" para rastrear con la pantalla apagada. El chofer la controla: la inicia al salir y la detiene al terminar.
- Este es el **código fuente**. Para instalarlo hay que compilarlo en Android Studio (Build → APK). No es un APK listo.

## Próximos pasos posibles

- Historial de rutas (guardar puntos cada X minutos y dibujar el recorrido).
- Alertas de geocerca (aviso al entrar/salir de un cliente).
- Login por chofer con Firebase Auth.
