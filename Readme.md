# 🚀 Sistema de Gestión de Usuarios, Solicitudes, Tickets y Comprobantes

Este sistema ha sido diseñado para la gestión de **usuarios**, **solicitudes**, **tickets** y **comprobantes** en un entorno colaborativo y de servicios empresariales. Incluye módulos específicos para roles administrativos, personal del centro de servicios y usuarios empresariales.

---

## 🗂️ **Módulos Principales**

### 🧑‍💻 **Usuarios**
- Gestión de la información personal, cambio de contraseñas y eliminación de cuentas.
- Roles jerarquizados con acceso a funcionalidades específicas.

### 📋 **Solicitudes**
- Creación, modificación y eliminación de solicitudes empresariales.
- Manejo avanzado de estados como **PENDIENTE**, **ACEPTADO** y **RECHAZADO**.

### 🎟️ **Tickets**
- Generación automática basada en solicitudes aceptadas.
- Filtrado por prioridad y estado, manteniendo un historial detallado.

### 🧾 **Comprobantes de Pago**
- Gestión de comprobantes con soporte para filtros de estado de pago.
- Generación de documentos PDF para análisis y exportación.

---

## 📖 **Documentación de la API**

Accede a la documentación completa, incluyendo descripciones detalladas y ejemplos, en este enlace:  
[📄 **Documentación en Postman**](https://documenter.getpostman.com/view/34383022/2sAYHwKQaJ)

---

## 📂 **Estructura del Proyecto**

```plaintext
src/
├── main/
│   ├── gen/
│   │   └── java/
│   │       └── com.tesis.tigmotors/
│   │           ├── Config/          # Configuración general del proyecto
│   │           ├── controller/      # Controladores REST de cada módulo
│   │           ├── converters/      # Conversión entre entidades y DTOs
│   │           ├── dto/             # Objetos de transferencia de datos
│   │           ├── enums/           # Enumeraciones para estados y prioridades
│   │           ├── Exceptions/      # Manejo centralizado de excepciones
│   │           ├── Jwt/             # Lógica de autenticación y JWT
│   │           ├── models/          # Entidades del dominio del sistema
│   │           ├── repository/      # Repositorios para la interacción con la base de datos
│   │           ├── security/        # Configuración y filtros de seguridad
│   │           ├── service/         # Lógica de negocio y servicios
│   │           ├── utils/           # Utilidades compartidas
│   │           └── TigMotorsApplication.java # Clase principal del proyecto
│   ├── resources/
│       ├── templates/               # Plantillas de email u otros documentos
│       ├── application.properties   # Configuración de la aplicación
│       └── banner.txt               # Banner personalizado al iniciar la aplicación
```

---

## 🚀 **Cómo Iniciar**

1. Clona este repositorio:  
   ```bash
   git clone https://github.com/JosephYM07/Backend_titulacion.git
   ```
2. Configura tus credenciales en el archivo `application.properties`.
3. Ejecuta la aplicación con Maven:  
   ```bash
   mvn spring-boot:run
   ```
4. Accede a la aplicación en:  
   `http://localhost:8080`

---

## 🛠️ **Características Técnicas**
- **Framework Backend**: Spring Boot con arquitectura REST.
- **Autenticación**: Seguridad implementada con JWT.
- **Base de Datos**: MySQL para almacenamiento de datos de autenticacion y MongoDB para gestion de solicitudes, tickets y facturas.
- **Validación y Manejo de Errores**: Centralizado con `GlobalExceptionHandler`.
- **Diseño Modular**: Separación de responsabilidades por capas.

---

## 🛡️ **Roles en el Sistema**
1. **ADMIN**:
   - Gestión completa de usuarios, solicitudes, tickets y facturas.
   - Información Personal
2. **PERSONAL_CENTRO_DE_SERVICIOS**:
   - Administración de tickets y facturas.
   - Información Personal
3. **USER**:
   - Gestión de solicitudes y visualización de tickets y facturas.
   - Información Personal
---
