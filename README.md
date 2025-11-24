PurrfectStore — Android App (Kotlin + XML + Xano)

Aplicación Android nativa que consume un backend de Xano para ofrecer un catálogo de productos, autenticación persistente con roles, carrito de compras, flujo de pago simulado y módulo administrativo para gestión de productos, usuarios y órdenes.

Características principales
Autenticación y Sesión

Login y registro mediante correo y contraseña, consumo de /auth/login y /auth/signup.

Persistencia del token, ID de usuario y rol con SharedPreferences (TokenManager).

Detección automática de sesión activa desde MainActivity.

Redirección dinámica por rol a HomeActivity (Cliente o Admin).

Cierre de sesión seguro limpiando preferencias.

Catálogo y Detalle de Productos

Listado dinámico con RecyclerView y ProductAdapter.

Vista cliente y vista administrador diferenciadas con layouts y acciones distintas.

Búsqueda de productos según API de Xano.

Vista de detalle (fragment_product_details.xml) con información completa del producto y carrusel de imágenes con ImageSlideAdapter.

Carrito y Pago Simulado

Carrito gestionado desde el fragmento fragment_cart.xml usando CartAdapter.

Edición de cantidades, eliminación de ítems y subtotal dinámico.

Pago simulado que crea un registro de orden en el backend (/cart).

Flujo de confirmación de pago, cambio de estado y solicitud de envío.

Órdenes (Cliente y Admin)

Cliente: visualización de historial en MyOrdersAdapter (fragment_my_orders.xml).

Cliente: detalle de orden en OrderDetailsProductAdapter y OrderDetailsAdapter.

Admin: vista de órdenes pendientes, aceptadas y rechazadas (fragment_order_details.xml).

Admin: actualización de estado usando PATCH a /cart/{id} (aceptado / rechazado).

Gestión de Productos (Admin)

CRUD completo usando los endpoints /product y /upload/image.

Agregar productos con múltiples imágenes usando ImagePreviewAdapter.

Edición y eliminación con validaciones.

Vista exclusiva de administrador (fragment_products_admin.xml).

Gestión de Usuarios (Admin)

Listado completo con UserAdapter (fragment_users_list.xml).

Crear y editar usuarios (fragment_add_users.xml, fragment_edit_users.xml).

Bloquear / desbloquear usuarios mediante el campo active.

Detalle de cada usuario y sus órdenes (fragment_user_order_list.xml y UserOrderListAdapter).

Navegación

Implementada mediante DrawerActivity (HomeActivity) con menús dinámicos según rol.

Fragments independientes organizados por contexto (cliente / admin).

AppBar y Drawer actualizados según navegación.

Tecnologías y versiones
Android

Kotlin (1.9+)

compileSdk 34

targetSdk 34

minSdk 24

viewBinding habilitado

AndroidX y UI

RecyclerView

Navigation por Drawer

Material Components

ViewPager2 (carrusel de imágenes)

Snackbar / Toast para feedback

Networking

Retrofit + Gson converter

OkHttp para peticiones HTTP

TokenManager para adjuntar Authorization: Bearer <token>

Upload de imágenes en multipart usando /upload/image

Imágenes

Uso de URLs devueltas por Xano Files

Múltiples imágenes por producto

ImageSlideAdapter e ImagePreviewAdapter

Arquitectura y flujo
Capas principales
Activities
Activity	Función
MainActivity	Verifica sesión y redirige por rol
HomeActivity	Activity principal con Drawer y fragmentos dinámicos
RegisterActivity	Registro de nuevos usuarios
Fragments (Cliente)

fragment_product.xml

fragment_product_details.xml

fragment_cart.xml

fragment_payment.xml

fragment_my_orders.xml

fragment_order_details.xml

fragment_profile.xml

fragment_edit_profile.xml

Fragments (Admin)

fragment_products_admin.xml

fragment_add_product.xml

fragment_edit_product.xml

fragment_users_list.xml

fragment_add_users.xml

fragment_edit_users.xml

fragment_user_order_list.xml

fragment_user_order_details.xml

fragment_product_details_order.xml

Adapters

ProductAdapter

CartAdapter

ImagePreviewAdapter

ImageSlideAdapter

MyOrdersAdapter

OrderDetailsAdapter

OrderDetailsProductAdapter

UserAdapter

UserOrderListAdapter

Manejo de Sesión

TokenManager.kt

Guarda: token, userId, userRole

Expone helpers: saveToken(), getToken(), isLoggedIn()

Usado en Activities y Retrofit

Redistribución del rol

Login

TokenManager guarda token y rol

MainActivity:

Si token existe → /auth/me

Si válidos → abrir HomeActivity

Si admin → menu admin

Si cliente → menu cliente

Backend Xano
URLs Base

Auth: https://x8ki-letl-twmt.n7.xano.io/api:8Cd9QvL_

E-commerce: https://x8ki-letl-twmt.n7.xano.io/api:4LX8pHTM

Endpoints

Auth: /auth/login, /auth/signup, /auth/me
Productos: /product, /product/{id}
Usuarios: /user, /user/{id}
Órdenes: /cart, /cart/{id}
Imágenes: /upload/image

Estados de orden

Cliente: pendiente

Admin: aceptado, rechazado

com.mycat.purrfectstore2/
├─ app/
│  ├─ src/main/
│  │  ├─ java/com/mycat/purrfectstore2/
│  │  │  ├─ api/        # Retrofit, servicios, interceptores y lógica de red
│  │  │  ├─ model/      # Modelos y DTOs (Auth, Productos, Carrito, Usuarios)
│  │  │  └─ ui/         # Activities, Fragments y Adapters (cliente y admin)
│  │  ├─ res/           # Layouts XML, drawables, menús, temas y strings
│  │  └─ AndroidManifest.xml
│  └─ build.gradle.kts  # Configuración del módulo (Retrofit, ViewBinding, etc.)
│
├─ gradle/              # Wrapper y catálogos de versiones
├─ build.gradle.kts     # Build principal del proyecto
└─ settings.gradle.kts  # Declaración del módulo :app

Instalación y ejecución
Requisitos

Android Studio Ladybug o superior

JDK 17

Android SDK 24+

Pasos

Clonar repo:

git clone https://github.com/Vi-lineros/PurrfectStore2


Abrir en Android Studio

Sincronizar Gradle

Ejecutar app en emulador o dispositivo

Credenciales de prueba

Administrador
Correo: admin@gmail.com
Contraseña: admin123

Cliente
Correo: cliente@gmail.com
Contraseña: cliente123

APK
Descargar APK:
https://github.com/Vi-lineros/PurrfectStore2/releases/download/v1.0/app-release.apk

Autor

Vicente Lineros
Duoc UC — Ingeniería Informática
2025
