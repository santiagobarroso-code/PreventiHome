# PreventiHome

> Aplicación Android para la gestión de citas, consultas y planes de ejercicio entre pacientes y fisioterapeutas.

Proyecto individual desarrollado para la materia **Desarrollo de Aplicaciones** (UDLAP). PreventiHome digitaliza el seguimiento de tratamientos de fisioterapia: los pacientes agendan citas y siguen sus ejercicios, los fisioterapeutas gestionan consultas y prescriben rutinas, y los administradores supervisan el sistema.

---

## Demo

<!-- Agrega aquí capturas de cada rol, por ejemplo: -->
| Login | Home Paciente | Home Fisio | Panel Admin |
|-------|---------------|------------|--------------|
| ![login](screenshots/login.png) | ![paciente](screenshots/paciente_home.png) | ![fisio](screenshots/fisio_home.png) | ![admin](screenshots/admin.png) |

---

## Roles y funcionalidades

### Paciente
- Agendar citas con un fisioterapeuta (`AgendarCitaFragment`)
- Ver historial de citas y consultas (`MisCitasFragment`, `HistorialFragment`)
- Ver detalle de cada consulta (`DetalleConsultaFragment`)
- Seguir ejercicios asignados con series, repeticiones y nivel de dificultad (`EjerciciosFragment`, `EjercicioDetalleFragment`)
- Registrar y visualizar su progreso (`ProgresoViewModel`)
- Gestionar su perfil (`PerfilFragment`)

### Fisioterapeuta
- Ver panel de inicio con resumen de actividad (`FisioHomeFragment`)
- Gestionar citas asignadas (`CitasFisioFragment`)
- Crear consultas a partir de una cita, con diagnóstico y patología (`CrearConsultaFragment`)
- Ver historial de pacientes atendidos (`HistorialFisioFragment`)
- Ver el listado de sus pacientes (`PacienteAdapter`)

### Administrador
- Gestión de usuarios del sistema (`AdminFragment`, `UsuarioAdminAdapter`)

---

## Arquitectura

```
UI (Fragments)
     ↕
ViewModel (MVVM + Lifecycle)
     ↕
Repository (capa de dominio)
     ↕
Remote Source (Firebase Auth / Firestore)
```

- **Patrón:** MVVM con separación clara entre `ui/`, `viewmodel/`, `domain/model/` y `data/repository/`
- **Inyección de dependencias:** Hilt (`AppModule`)
- **Persistencia y backend:** Firebase (sin servidor propio)

---

## Stack Tecnológico

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje principal |
| Android Studio (Gradle Kotlin DSL) | Entorno de desarrollo |
| Hilt | Inyección de dependencias |
| Firebase Auth | Autenticación (incluye Google Sign-In) |
| Cloud Firestore | Base de datos en tiempo real |
| Firebase Storage | Almacenamiento de imágenes/archivos |
| Firebase Cloud Messaging | Notificaciones push |
| Firebase Analytics | Métricas de uso |
| Biometric (androidx.biometric) | Autenticación biométrica |
| Navigation Component | Navegación entre fragments |
| ViewBinding | Acceso a vistas sin `findViewById` |

---

## Modelo de Datos

```
User        → uid, email, nombre, rol ("paciente" | "fisio" | "admin")
Cita        → id, pacienteId, pacienteNombre, pacienteEmail, fisioId,
               fechaCita, motivo, estado ("pendiente" | "atendida" | "cancelada"),
               consultaId, creadaEn
Consulta    → (generada a partir de una Cita atendida por el fisio)
Ejercicio   → id, nombre, descripcion, zona, tipo, dificultad,
               series, repeticiones, imagenUrl, activo
Progreso    → (seguimiento del paciente sobre sus ejercicios asignados)
```

---

## Estructura del Proyecto

```
PreventiHome/
└── app/src/main/java/com/example/preventihome/
    ├── ui/
    │   ├── auth/          # Login y registro
    │   ├── patient/       # Pantallas del rol paciente
    │   ├── physio/         # Pantallas del rol fisioterapeuta
    │   ├── admin/          # Pantallas del rol administrador
    │   └── MainActivity.kt
    ├── viewmodel/          # ViewModels por dominio (Auth, Cita, Consulta, Ejercicio, Progreso, Admin, Perfil)
    ├── domain/model/       # Modelos de dominio (User, Cita, Consulta, Ejercicio, Progreso)
    ├── data/
    │   ├── repository/    # Lógica de acceso a datos por dominio
    │   └── remote/         # Fuentes remotas (FirebaseAuthSource, FirestoreSource)
    ├── di/                 # Módulo de Hilt
    └── utils/              # Utilidades (ej. Prefs)
```

---

## Instalación y Configuración

**1.** Clona el repositorio:
```bash
git clone https://github.com/santiagobarroso-code/PreventiHome.git
```

**2.** Abre la carpeta del proyecto en Android Studio.

**3.** Descarga tu propio `google-services.json` desde [Firebase Console](https://console.firebase.google.com/) (crea un proyecto y registra la app con el applicationId `com.example.preventihome`) y colócalo en `app/`.

**4.** Habilita en Firebase Console:
- Authentication → Email/Password y Google Sign-In
- Cloud Firestore
- Storage
- Cloud Messaging

**5.** Sincroniza Gradle y ejecuta la app en un emulador o dispositivo (minSdk 26).

---

## Notas

- Proyecto desarrollado de forma individual para la materia Desarrollo de Aplicaciones (UDLAP).
- No incluye backend propio: toda la lógica de datos vive en Firebase.
