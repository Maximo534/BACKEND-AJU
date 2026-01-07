# prueba-api-rest

Proyecto back con arquitectura hexagonal que sirve de base

### Requisitos Iniciales

| Herramienta     | Version                           |
|:----------------|:----------------------------------|
| Java            | 21.x                              |
| Springboot      | 3.x
| Maven           | 3.x                               |
| Lombok          | 1.x                               |

- Configurar Java, Maven y Lombok en una ruta local del espacio de trabajo.

### Contexto del servicio

- /prueba-api

### arquitectura

```
prueba-api-rest/
│
├── prueba-api-domain/
│   ├── src/main/java
│   │   ├── pe/gob/pj/[nombrecorto]/domain/common/
│   │   │   ├── enums/                                              # Enums comunes.
│   │   │   ├── constants/                                          # Constantes globales.
│   │   │   ├── utils/                                              # Clases utilitarias.
│   │   │   │
│   │   │
│   │   ├── pe/gob/pj/[nombrecorto]/domain/model/                   # Modelos y DTOs.
│   │   │   ├── negocio/
│   │   │   ├── [basedatos]/
│   │   │   ├── cliente/[servicioconsumir]/
│   │   │   │
│   │   │
│   │   ├── pe/gob/pj/[nombrecorto]/domain/port/                    # Interfaces de la funcionalidad a utilizar.
│   │   │   ├── client/[servicioconsumir]/                          # Servicios externos consumidos.
│   │   │   ├── persistence/                                        # Manejo de datos en BD.
│   │   │   ├── usecase/                                            # Casos de uso.
│   │   │   │
│   │   │
│   │   ├── pe/gob/pj/[nombrecorto]/domain/exceptions/              # Package donde va todas las excepciones personalizadas.
│   │   │   ├── TokenException.java
│   │   │   ├── CaptchaException.java
│   │   │   ├── ...
│   │   │   │
│   │
│   └── pom.xml                                                     # Archivo domain de configuración maven
│
├── prueba-api-usecase/
│   ├── src/main/java
│   │   ├── pe/gob/pj/[nombrecorto]/usecase/                        # Package donde van las clases que implementan ports usecase
│   │   │   ├── SeguridadUseCaseAdapter.java                        # Clase que implementa el caso de uso para seguridad
│   │   │   ├── ...
│   │   │   │
│   │
│   └── pom.xml                                                     # Archivo usecase de configuración maven               
│
├── prueba-api-infrastructure/
│   ├── src/main/java
│   │   ├── pe/gob/pj/[nombrecorto]/infrastructure/async/           # Package donde va todo referente al manejo de asyncronia
│   │   │   ├── AsyncConfig.java                                    # Clase de configuración para async
│   │   │   ├── ...
│   │   │   │
│   │   │   
│   │   ├── pe/gob/pj/[nombrecorto]/infrastructure/client/          # Package donde va todo referente al consumo de servicios
│   │   │   ├── RestTemplateConfig.java                             # Clase de configuración para resttemplate.
│   │   │   ├── ...
│   │   │   │
│   │   │
│   │   ├── pe/gob/pj/[nombrecorto]/infrastructure/db/              # Package donde va todo referente al consumo de base de datos
│   │   │   ├── [basedatos]Config.java                              # Clase de configuración a bd a manejar Ej: SeguridadConfig.java
│   │   │   ├── [basedatos]/entities/                               # Package donde va las clases entidades
│   │   │   ├── [basedatos]/repositories/                           # Package donde va los objetos reposiotry y dsl de las entidades
│   │   │   ├── persistence/                                        # Package donde va las clases que implementan ports persistence
│   │   │   ├── ...
│   │   │   │
│   │   │
│   │   ├── pe/gob/pj/[nombrecorto]/infrastructure/mappers/         # Package donde van las interfaces que sirven para mapear clases
│   │   │   ├── ...
│   │   │   │
│   │   │
│   │   ├── pe/gob/pj/[nombrecorto]/infrastructure/documentation/   # Package donde va todo referente a documentación de endpoints
│   │   │   ├── SwaggerConfig.java                                  # Clase de configuración de swagger
│   │   │   ├── ...
│   │   │   │
│   │   │
│   │   ├── pe/gob/pj/[nombrecorto]/infrastructure/rest/            # Package donde va todo referente a los endpoints.
│   │   │   ├── advises/                                            # Package donde va interceptor para manejar excepciones
│   │   │   ├── controllers/                                        # Package donde va interfaces y clases de endpoints
│   │   │   ├── requests/                                           # Package donde va las clases request de las peticiones.
│   │   │   ├── responses/                                          # Package donde va las clases response de las peticiones.
│   │   │   ├── ...
│   │   │   │
│   │   │
│   │   ├── pe/gob/pj/[nombrecorto]/infrastructure/security/        # Package donde va todo referente a la seguridad.
│   │   │   ├── SecurityConfig.java                                 # Clase de configuración de spring security
│   │   │   ├── adapters/
│   │   │   │   └── UserDetailsServiceAdapter.java
│   │   │   └── filters/                                            # Package donde van los filtros de seguridad
│   │   │       ├── JwtAuthenticationFilter.java                    # Clase encargada de validar la autenticación de los parámetros
│   │   │       └── JwtAuthorizationFilter.java                     # Clase encargada de validar la autorización a los endpoints
│   │   │   
│   │   ├── ...
│   │
│   └── pom.xml                                                     # Archivo infraestructure de configuración maven
│
└── pom.xml                                                         # Archivo general de configuración de Maven.

```

### Base de Datos

| Entorno     | Tipo de BD        | Servidor        |Puerto|BD                  |Usuario del Servicio|
|:------------|:------------------|:----------------|:-----|:-------------------|:-------------------|
| Desarrollo  | PostgreSQL        |172.18.11.241    |39969 |Seguridad           |uc_prueba           |
| Desarrollo  | PostgreSQL        |172.18.11.241    |39969 |AUDITORIA_GENERAL   |uc_prueba           |
| Desarrollo  | PostgreSQL        |172.18.11.241    |39969 |PRUEBA              |uc_prueba           |

### Recursos 

+ Los recursos corresponden a los archivo propeties y logs, y se debe ubicar en la carpeta modules/pe/gob/pj/prueba del servidor donde se despliega el proyecto.

+ Para obtener los recursos con git bash ejecutar el siguiente comando: 
```
git clone https://desagit.pj.gob.pe/sdsi/recurso-servicios-in-modules/prueba/api.git
```

### URLs del servicio

| Nombre        | Link Url                                                       |
|:--------------|:---------------------------------------------------------------|
|Url Base       |http://172.19.9.35:8080/prueba-api-rest                         |
|Documentación  |http://172.19.9.35:8080/prueba-api-rest/swagger-ui/index.html   |
|Disponibilidad |http://172.19.9.35:8080/prueba-api-rest/healthcheck             |

