# Gym App Backend

A simplified Spring Boot backend for the Gym App.

## Local Development

### Prerequisites

- Java 17
- Maven
- Docker (optional)

### Running Locally

1. Clone the repository
2. Navigate to the project directory
3. Run the application:

```bash
./mvnw spring-boot:run
```

Or with Docker:

```bash
docker-compose up --build
```

The application will be available at http://localhost:8082/api

### Testing

Test the application by accessing:
```
http://localhost:8082/api/public/test
```

## Deployment

### Deploying to Render

1. Create a Render account at https://render.com
2. Connect your GitHub repository
3. Create a new Web Service
4. Select the repository and branch
5. Configure the service:
   - Name: gymapp-backend
   - Environment: Java
   - Build Command: `./mvnw clean package -DskipTests`
   - Start Command: `java -jar target/backend-0.0.1-SNAPSHOT.jar`
   - Add environment variables:
     - ADMIN_PASSWORD: (generate a secure password)
     - SPRING_PROFILES_ACTIVE: prod

### Deploying to Railway

1. Create a Railway account at https://railway.app
2. Connect your GitHub repository
3. Create a new project
4. Add a new service from your GitHub repo
5. Configure the service:
   - Build Command: `./mvnw clean package -DskipTests`
   - Start Command: `java -jar target/backend-0.0.1-SNAPSHOT.jar`
   - Add environment variables:
     - ADMIN_PASSWORD: (generate a secure password)
     - SPRING_PROFILES_ACTIVE: prod

## API Endpoints

- `GET /api/public/test` - Test endpoint (public)
- More endpoints will be added as needed 