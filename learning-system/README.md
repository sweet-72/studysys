# SmartClass Backend

Spring Boot backend for the SmartClass learning platform.

## Tech Stack

- Java 17
- Spring Boot 2.7.2
- MyBatis-Plus 3.5.2
- MySQL 8
- Redis
- Elasticsearch 7.17.x
- Knife4j API documentation
- Netty WebSocket
- Dify API integration
- Tencent COS optional file storage with local upload fallback

## Directory Structure

```text
learning-system/
├── src/main/java/com/ttbt/smartclass/
│   ├── controller/      # REST controllers
│   ├── service/         # service interfaces and implementations
│   ├── mapper/          # MyBatis mapper interfaces
│   ├── model/           # entity, DTO, VO, enum
│   ├── config/          # Spring configuration
│   ├── manager/         # infrastructure managers
│   ├── netty/           # WebSocket server
│   └── utils/           # utility classes
├── src/main/resources/
│   ├── mapper/          # MyBatis XML files
│   └── application-example.yml
├── sql/smart_class.sql  # database schema and demo data
├── upload/              # local demo/runtime upload directory
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Local Startup

### 1. Prepare Configuration

```bash
copy src\main\resources\application-example.yml src\main\resources\application.yml
```

Edit `application.yml` and configure MySQL, Redis, Elasticsearch, Dify, COS, and WeChat settings as needed.

### 2. Initialize Database

```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS smart_class DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -p smart_class < sql/smart_class.sql
```

### 3. Start Dependencies

Make sure these services are available:

- MySQL: `localhost:3306`
- Redis: `localhost:6379`
- Elasticsearch: `localhost:9200` if search features are enabled

### 4. Run Backend

```bash
mvn spring-boot:run
```

Or build and run the jar:

```bash
mvn clean package -DskipTests
java -jar target/*.jar
```

## API URLs

- Base API: `http://localhost:8101/api`
- Knife4j: `http://localhost:8101/api/doc.html`
- WebSocket port: configured by `netty.websocket.port`, default `12346` in the example config.

## Docker Startup

```bash
docker compose up -d
```

The compose file starts the backend, MySQL, Redis, and Elasticsearch. MySQL imports `sql/smart_class.sql` on first initialization.

## Upload Directory

`upload/` is used by local file upload fallback and demo course resources. For a public repository, keep only intentional demo resources and `.gitkeep` files. Real user-uploaded files should not be committed.

## Security Notes

- Do not commit real `application.yml` files.
- Replace placeholder Dify, COS, WeChat, Redis, and database credentials before production.
- The SQL file contains demo accounts only and should not be used as production data.
