PurrfectStore — App Android (Kotlin + XML)

E-commerce móvil con roles (Cliente / Admin), sesión persistente y gestión de productos, usuarios y órdenes.
Proyecto final para Programación de Aplicaciones Móviles (Duoc UC).

1. Descripción General

PurrfectStore es una aplicación Android desarrollada en Kotlin usando layouts XML y consumiendo un backend REST creado en Xano.

La app implementa dos roles principales:

Cliente: catálogo, carrito, pago simulado, solicitud de envío y edición de perfil.

Admin: CRUD de productos, CRUD de usuarios y aprobación o rechazo de órdenes.

Incluye autenticación, persistencia de sesión, navegación dinámica según rol y subida de múltiples imágenes por producto.

2. Tecnologías Utilizadas
Frontend (Android)

Kotlin

XML Layouts

ViewBinding

DrawerActivity + Fragments

RecyclerView

SharedPreferences

Retrofit + Gson

Validaciones y manejo de errores

Ícono personalizado

Backend

Xano (REST + Files)

Almacenamiento de imágenes en el módulo Files de Xano

3. Backend (Xano)
URLs Base

Autenticación: https://x8ki-letl-twmt.n7.xano.io/api:8Cd9QvL_

E-commerce: https://x8ki-letl-twmt.n7.xano.io/api:4LX8pHTM

4. Endpoints Utilizados

AUTH
- Login	POST	/auth/login
- Registro	POST	/auth/signup
- Perfil autenticado	GET	/auth/me

PRODUCTOS
- Listar productos	GET	/product
- Crear producto	POST	/product
- Obtener producto	GET	/product/{product_id}
- Editar producto	PATCH	/product/{product_id}
- Eliminar producto	DELETE	/product/{product_id}

USUARIOS (Admin)
- Listar usuarios	GET	/user
- Crear usuario	POST	/user
- Obtener usuario	GET	/user/{user_id}
- Editar usuario	PATCH	/user/{user_id}
- Eliminar usuario	DELETE	/user/{user_id}

CARRITO / ÓRDENES
- Acción	Método	Endpoint
- Listar carritos	GET	/cart
- Crear carrito	POST	/cart
- Obtener carrito	GET	/cart/{cart_id}
- Editar carrito / estado	PATCH	/cart/{cart_id}
- Eliminar carrito	DELETE	/cart/{cart_id}
- (En esta aplicación los carritos funcionan también como órdenes)

Estados utilizados:

Cliente: en proceso, pendiente

Admin: aceptado, rechazado

IMÁGENES
Acción	Método	Endpoint
Subir archivo	POST	/upload/image

Las imágenes quedan almacenadas en Xano (Files).

5. Instalación y Configuración del Proyecto
Requisitos

Android Studio Ladybug o superior

SDK 24+

Emulador o dispositivo físico Android

Pasos para compilar

Clonar el repositorio:

git clone https://github.com/Vi-lineros/PurrfectStore2.git


Abrir en Android Studio.

Esperar a que Gradle sincronice.

No requiere claves ni archivos secretos.

Ejecutar en emulador o dispositivo físico.

6. Credenciales de Prueba
Administrador

Correo: admin@gmail.com

Contraseña: admin123

Cliente

Correo: cliente@gmail.com

Contraseña: cliente123

7. Funcionalidades Implementadas

Cliente

✔ Buscar y listar productos

✔ Ver detalles del producto

✔ Agregar al carrito

✔ Editar cantidades

✔ Eliminar ítems del carrito

✔ Pago simulado

✔ Solicitar envío (estado: pendiente)

✔ Ver estado de pedido

✔ Editar perfil

✔ Cerrar sesión

Admin

✔ Crear productos

✔ Editar productos

✔ Eliminar productos

✔ Subir múltiples imágenes

✔ Listar productos

✔ Crear / editar / eliminar usuarios

✔ Bloquear usuarios

✔ Ver solicitudes de pagos/envíos

✔ Actualizar estado de la orden (aceptado / rechazado)

✔ Cerrar sesión

8. Arquitectura General

El proyecto utiliza:

DrawerActivity como contenedor principal

Fragments para las pantallas

Managers para comunicación con Retrofit

SharedPreferences para token, rol y estado de sesión

RecyclerView para productos, usuarios y carritos/órdenes

9. Funcionamiento de la Sesión

Al iniciar sesión se guarda el token y el rol.

Desde el Splash se redirige automáticamente según el rol.

El logout limpia completamente las SharedPreferences.

10. Subida de Imágenes

Envío de archivos por multipart/form-data.

Xano retorna la URL pública.

La app guarda la URL asociada a cada producto.

11. APK

(Agregar aquí el enlace cuando el archivo sea subido al repositorio.)

12. Autor

Vicente Lineros
Duoc UC — Ingeniería Informática
2025
