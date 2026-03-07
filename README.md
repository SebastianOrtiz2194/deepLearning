# 🧠 Deep Learning Asynchronous Backend

Un backend robusto, escalable y orientado a eventos diseñado para servir modelos de Machine Learning y Deep Learning pesados sin bloquear las peticiones de los usuarios. Utiliza Spring Boot como orquestador, Apache Kafka para el encolamiento de tareas, Redis para el manejo de estados rápidos y Deep Java Library (DJL) para la inferencia nativa en Java.

## ✨ Características Principales

- Procesamiento Asíncrono: Las peticiones de inferencia no bloquean el hilo principal del servidor.

- Escalabilidad Horizontal: Los workers (consumidores de Kafka) pueden escalar independientemente del API Gateway.

- Alta Disponibilidad: Uso de Kafka para garantizar la entrega de mensajes y evitar pérdida de tareas en caso de caídas.

- Inferencia Nativa: Integración con modelos de PyTorch, TensorFlow o MXNet directamente en la JVM a través de DJL. 

## 📋 Prerrequisitos

- Java 17 o superior

- Maven 3.8+ o Gradle

- Docker y Docker Compose

## Instalación y Ejecución Local

### 1. Clonar el repositorio

```
git clone [https://github.com/tu-usuario/dl-async-backend.git](https://github.com/tu-usuario/dl-async-backend.git)
cd dl-async-backend
```


### 2. Levantar la Infraestructura (Kafka, Zookeeper y Redis)
Utilizamos Docker Compose para levantar los servicios de mensajería y caché localmente.

```
docker-compose up -d
```


Verifica que los contenedores estén corriendo con docker ps.

### 3. Compilar y Ejecutar la Aplicación Spring Boot

```
./mvnw clean install
./mvnw spring-boot:run
```


La API estará disponible en http://localhost:8080.
