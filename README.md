# Microservicio de configuración

Servicio Spring Boot para consultar y actualizar parámetros JSONB de cada negocio en `rm_detalles.configuraciones_negocio`. Resuelve el negocio usando `X-Negocio-Codigo`, valida la membresía del usuario y exige el permiso `CONFIGURACION_GESTIONAR`. Las consultas y cambios siempre se limitan al `negocio_id` de la solicitud.

## Tecnologías

- Java 21 y Spring Boot 4.1.2
- Spring MVC, Spring JDBC y PostgreSQL
- Spring Security Resource Server con JWT de Supabase Auth (ES256)
- CORS con orígenes activos de `rm_detalles.frontends_negocio`

## Ejecutar localmente

1. Abre esta carpeta como proyecto Maven en IntelliJ IDEA.
2. Selecciona JDK 21.
3. Configura las variables de `.env.example` en **Run > Edit Configurations > Environment variables**. Spring Boot no carga automáticamente un archivo `.env`.
4. Ejecuta `ConfiguracionServiceApplication`; el puerto por defecto es `8083`.

El servicio usa la estructura multiempresa existente y no crea ni altera tablas. `ddl-auto: validate` valida las entidades JPA usadas para negocios y frontends.

## Seguridad y headers

Todas las rutas de configuración requieren:

```http
Authorization: Bearer <access_token>
X-Negocio-Codigo: RMD
Content-Type: application/json
```

El usuario debe tener membresía activa en el negocio y el permiso `CONFIGURACION_GESTIONAR`. En navegador, el origen también debe estar activo y asociado al negocio en `frontends_negocio`.

## Endpoints

| Método | Ruta | Uso |
|---|---|---|
| GET | `/api/v1/configuracion/parametros` | Lista los parámetros del negocio actual. |
| GET | `/api/v1/configuracion/parametros/{clave}` | Consulta un parámetro, por ejemplo `general.moneda`. |
| PUT | `/api/v1/configuracion/parametros/{clave}` | Actualiza el valor JSONB de un parámetro existente. |
| GET | `/api/v1/configuracion/frontends` | Lista los orígenes registrados en el negocio. |
| POST | `/api/v1/configuracion/frontends` | Registra un origen para el negocio actual. |
| PUT | `/api/v1/configuracion/frontends/{id}` | Actualiza un origen o su estado. |
| DELETE | `/api/v1/configuracion/frontends/{id}` | Desactiva un origen. |
| GET | `/actuator/health` | Health check público. |

Ejemplo para cambiar el máximo de imágenes por producto:

```http
PUT http://localhost:8083/api/v1/configuracion/parametros/catalogo.imagenes.max_por_producto
```

```json
{
  "valor": 6
}
```

El endpoint modifica únicamente una clave que ya exista para el negocio. Las claves habilitadas tienen validación de tipo y rango: moneda de tres letras mayúsculas, zona horaria IANA, máximo de imágenes de 1 a 20, tamaño de imagen de 1 a 10 MB, lista no vacía de MIME JPG/PNG/WebP y descuento máximo de 0 a 100.

Ejemplo de registro de un frontend local:

```json
{
  "origen": "http://localhost:4201",
  "hostPublico": "localhost",
  "activo": true
}
```

Los orígenes se guardan como URL de origen exacta, sin ruta. El servicio CORS de los microservicios consulta esta tabla; activar o desactivar un origen afecta las próximas solicitudes sin cambiar código.

## Docker

```bash
mvn clean package
docker build -t configuracion-service .
```

El puerto del contenedor es `8083`. Configura las mismas variables de entorno al iniciar el contenedor.
