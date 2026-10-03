# SIS 1 — Task REST API

Курсовой проект: REST API на Spring Boot с CRUD-эндпоинтами, DTO и Bean Validation,
единым форматом ошибок, корректными HTTP-статусами, Maven-профилями и
HTTP-client коллекцией. Предметная область — управление задачами (`Task`).

## Стек

- Java 17, Spring Boot 3.3.4
- Spring Web (MVC), Spring Data JPA, Bean Validation (Hibernate Validator)
- H2 (dev/test), PostgreSQL-драйвер (prod)
- Lombok
- Maven (профили `dev` / `prod` / `test`)

## Структура проекта (поэтапно)

Проект собирался последовательно, слой за слоем — так его удобно и защищать по этапам:

1. **`pom.xml`** — зависимости и Maven-профили (`dev`, `prod`, `test`), включена
   фильтрация ресурсов, чтобы `application.yml` подставлял активный профиль.
2. **`src/main/resources/*.yml`** — конфигурация: общий `application.yml` +
   профильные `application-dev.yml` / `application-prod.yml` / `application-test.yml`.
3. **`entity/`** — JPA-сущность `Task` и енамы `TaskStatus`, `TaskPriority`.
4. **`dto/`** — `TaskRequestDto` (с аннотациями Bean Validation),
   `TaskResponseDto`, `TaskStatusUpdateDto` — сущность никогда не отдаётся наружу напрямую.
5. **`mapper/`** — `TaskMapper`, преобразование entity ↔ DTO.
6. **`repository/`** — `TaskRepository extends JpaRepository`.
7. **`exception/`** — `ApiError` (единый формат ошибки), `ResourceNotFoundException`,
   `GlobalExceptionHandler` (`@RestControllerAdvice`) — маппинг всех исключений на
   нужные HTTP-статусы.
8. **`service/`** — `TaskService` + `TaskServiceImpl`, бизнес-логика и транзакции.
9. **`controller/`** — `TaskController`, пять CRUD-эндпоинтов.
10. **`http-client/tasks.http`** — коллекция запросов на все эндпоинты, включая
    сценарии ошибок (400/404).

```
src/main/java/com/example/sis1/
├── Sis1Application.java
├── controller/TaskController.java
├── dto/
│   ├── TaskRequestDto.java
│   ├── TaskResponseDto.java
│   └── TaskStatusUpdateDto.java
├── entity/
│   ├── Task.java
│   ├── TaskStatus.java
│   └── TaskPriority.java
├── exception/
│   ├── ApiError.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── mapper/TaskMapper.java
├── repository/TaskRepository.java
└── service/
    ├── TaskService.java
    └── impl/TaskServiceImpl.java
```

## Эндпоинты

| Метод | URL                        | Успех             | Ошибки                        |
|-------|----------------------------|--------------------|--------------------------------|
| POST  | `/api/tasks`                | `201 Created` (+ `Location`) | `400` (валидация), `409` (дубль `title`) |
| GET   | `/api/tasks`                | `200 OK` (страница) | —                              |
| GET   | `/api/tasks/{id}`           | `200 OK`             | `404`, `400` (bad id)          |
| PUT   | `/api/tasks/{id}`           | `200 OK`             | `404`, `400`, `409` (дубль `title`) |
| PATCH | `/api/tasks/{id}/status`    | `200 OK`             | `404`, `400`                   |
| DELETE| `/api/tasks/{id}`           | `204 No Content`     | `404`                          |

`GET /api/tasks` поддерживает `?status=NEW`, `?page=0&size=10&sort=dueDate,asc`.

**409 Conflict**: `title` уникален. При попытке создать или обновить задачу с уже
существующим (без учёта регистра) заголовком сервис бросает
`ResourceConflictException` до похода в БД; на всякий случай гонки запросов
`GlobalExceptionHandler` дополнительно перехватывает `DataIntegrityViolationException`
от уникального индекса на уровне схемы — оба пути возвращают единый формат ошибки
со статусом `409`.

## Единый формат ошибки

Все ошибки (валидация, 404, 400, 500) возвращаются в одном виде через
`GlobalExceptionHandler`:

```json
{
  "timestamp": "2026-09-27T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/tasks",
  "errors": [
    { "field": "title", "message": "Title length must be between 3 and 100 characters" }
  ]
}
```

Поле `errors` присутствует только при ошибках валидации (`@JsonInclude(NON_NULL)`).
`404` и `409` возвращаются в той же форме, просто без поля `errors`, например:

```json
{
  "timestamp": "2026-09-27T10:20:11",
  "status": 409,
  "error": "Conflict",
  "message": "A task with title 'Prepare SIS1 report' already exists",
  "path": "/api/tasks"
}
```

## Запуск

```bash
# dev-профиль (по умолчанию): H2 in-memory, H2-консоль на /h2-console
mvn spring-boot:run

# prod-профиль: PostgreSQL (нужны переменные окружения DB_HOST/DB_NAME/DB_USERNAME/DB_PASSWORD)
mvn clean package -P prod
java -jar target/sis1-task-api.jar --spring.profiles.active=prod

# сборка и тесты под test-профилем
mvn clean test -P test
```

API поднимается на `http://localhost:8080`.

## Проверка через HTTP-client

Откройте `http-client/tasks.http` в IntelliJ IDEA (или VS Code с расширением
REST Client) и выполняйте запросы по порядку — коллекция покрывает все 6
эндпоинтов, включая сценарии `400` и `404`.
