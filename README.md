# PurrfectStore — Android App (Kotlin + XML + Xano)

Aplicación Android nativa desarrollada en Kotlin que consume un backend de Xano para ofrecer catálogo de productos, autenticación con roles, carrito con checkout remoto y un panel administrativo para la gestión de productos, usuarios y órdenes.

## Características principales
### Autenticación y sesión
- Login y registro mediante `/auth/login` y `/auth/signup`.
- Persistencia de token, ID de usuario y rol con `SharedPreferences` (TokenManager).
- Validación de sesión con `/auth/me`.
- Redirección según rol (cliente o administrador).
- Cierre de sesión seguro.
### Catálogo y detalle de productos
- Lista dinámica con `RecyclerView` y `ProductAdapter`.
- Vistas separadas para cliente y administrador.
- Búsqueda de productos desde la API.
- Detalle con carrusel de imágenes (`ImageSlideAdapter`).
### Carrito y pago simulado
- Manejo de carrito desde `fragment_cart.xml` con `CartAdapter`.
- Actualización de cantidades, eliminación y subtotal.
- Pago simulado creando una orden con `/cart`.
### Órdenes (cliente y admin)
- Cliente: historial y detalle de órdenes.
- Admin: panel de pedidos (pendientes, aceptados, rechazados).
- Actualización del estado de una orden con `PATCH /cart/{id}`.
### Gestión de productos (administrador)
- CRUD completo con `/product` y `/product/{id}`.
- Subida de imágenes vía `/upload/image`.
- Múltiples imágenes mediante `ImagePreviewAdapter`.
- Pantallas exclusivas para admin.
### Gestión de usuarios (administrador)
- Listado completo, creación, edición y bloqueo/desbloqueo.
- Vista detallada del usuario y sus órdenes.
### Navegación
- Drawer dinámico basado en el rol desde `HomeActivity`.
- Navegación modular por `fragments`.
- AppBar dinámico por sección.
## Tecnologías y versiones
### Android
- Kotlin 1.9+
- `compileSdk 34`, `targetSdk 34`, `minSdk 24`
- `ViewBinding` habilitado
### UI y AndroidX
- `RecyclerView`
- `Drawer Navigation`
- `ViewPager2`
- `Material Components`
### Networking
- `Retrofit` + `Gson`
- `OkHttp` con `interceptor`
- `TokenManager` para el Header `Authorization: Bearer <token>`
- `Multipart` para subir imágenes
### Imágenes
- URLs de `Xano Files`
- Carrusel con `ViewPager2`
## Arquitectura y flujo
| Activity        | Función                                   |
|-----------------|--------------------------------------------|
| MainActivity    | Verifica token y redirige según rol        |
| HomeActivity    | Drawer y navegación principal              |
| RegisterActivity| Registro de usuarios                       |       

### Fragments (Cliente)
- `fragment_product.xml`
- `fragment_product_details.xml`
- `fragment_cart.xml`
- `fragment_payment.xml`
- `fragment_my_orders.xml`
- `fragment_order_details.xml`
- `fragment_profile.xml`
- `fragment_edit_profile.xml`
### Fragments (Administrador)
- `fragment_products_admin.xml`
- `fragment_add_product.xml`
- `fragment_edit_product.xml`
- `fragment_users_list.xml`
- `fragment_add_users.xml`
- `fragment_edit_users.xml`
- `fragment_user_order_list.xml`
- `fragment_user_order_details.xml`
- `fragment_product_details_order.xml`
### Adapters
- `ProductAdapter`
- `CartAdapter`
- `ImagePreviewAdapter`
- `ImageSlideAdapter`
- `MyOrdersAdapter`
- `OrderDetailsAdapter`
- `OrderDetailsProductAdapter`
- `UserAdapter`
- `UserOrderListAdapter`
## Sesión
TokenManager.kt gestiona:
- `token`
- `userId`
- `userRole`
Consumido por `Activities` y capa de red.
## Backend Xano
### URLs base
- `e-commerce API`: https://x8ki-letl-twmt.n7.xano.io/api:4LX8pHTM
- `Authentication`: https://x8ki-letl-twmt.n7.xano.io/api:8Cd9QvL_
### Endpoints principales
- Autenticación: `/auth/login`, `/auth/signup`, `/auth/me`
- Productos: `/product`, `/product/{id}`
- Usuarios: `/user`, `/user/{id}`
- Órdenes: `/cart`, `/cart/{id}`
- Imágenes: `/upload/image`
### Estados de orden
- Cliente: `pendiente`
- Admin: `aceptado` / `rechazado`
## Estructura de carpetas

```txt
com.mycat.purrfectstore2/
├─ app/
│  ├─ src/main/
│  │  ├─ java/com/mycat/purrfectstore2/
│  │  │  ├─ api/        # Retrofit, servicios, interceptores y lógica de red
│  │  │  ├─ model/      # DTOs de auth, productos, carrito y usuarios
│  │  │  └─ ui/         # Activities, fragments y adapters (cliente/admin)
│  │  ├─ res/           # Layouts, menús, drawables, temas y strings
│  │  └─ AndroidManifest.xml
│  └─ build.gradle.kts  # Configuración del módulo (dependencias, ViewBinding, etc.)
├─ gradle/              # Wrapper y catálogos de versiones
├─ build.gradle.kts     # Configuración raíz
└─ settings.gradle.kts  # Declaración del módulo :app
```
## Instalación y ejecución
### Requisitos
- Android Studio Ladybug o superior
- JDK 17
- Android SDK 24+
## Pasos
### Clonar el repositorio
- git clone https://github.com/Vi-lineros/PurrfectStore2
- Abrir en Android Studio
- Sincronizar Gradle
- Ejecutar
### Credenciales de prueba
## Administrador
- Correo: `admin@gmail.com`
- Contraseña: `admin123`
## Cliente
- Correo: `cliente@gmail.com`
- Contraseña: `cliente123`
## APK
https://github.com/Vi-lineros/PurrfectStore2/releases/download/v1.0/app-release.apk
## Autor
### Vicente Lineros
### Duoc UC — Ingeniería Informática
