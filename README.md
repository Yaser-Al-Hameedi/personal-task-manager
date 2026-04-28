# Personal Task Manager

A REST API for managing tasks, built with Java 17, Spring Boot 3.4.5, and H2 (in-memory). Includes an AI-powered endpoint that converts plain-language descriptions into structured task objects using the OpenAI API.

---

## Requirements

- Java 17 or later
- Internet access (for Maven dependency download on first run and for the AI endpoint)

---

## Running the project

**1. Set your OpenAI API key** (required only for the `/tasks/suggest` endpoint):

```bash
export OPENAI_API_KEY=sk-...
```

**2. Start the server:**

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## Using the UI

Open `http://localhost:8080` in a browser. The UI lets you:

- View all tasks
- Create a new task
- Enter a plain-language description and get a structured task suggestion from the AI, then optionally save it

---

## API endpoints

| Method   | Path                | Description               |
|----------|---------------------|---------------------------|
| `POST`   | `/tasks`            | Create a task             |
| `GET`    | `/tasks`            | List all tasks            |
| `GET`    | `/tasks/{id}`       | Get a single task         |
| `PUT`    | `/tasks/{id}`       | Update a task             |
| `DELETE` | `/tasks/{id}`       | Delete a task             |
| `POST`   | `/tasks/suggest`    | AI-powered task suggestion |

---

## AI endpoint: `POST /tasks/suggest`

Accepts a plain-language description and returns a structured task object. The result is **not persisted** — use the create endpoint to save it.

**Request:**

```bash
curl -X POST http://localhost:8080/tasks/suggest \
  -H "Content-Type: application/json" \
  -d '{"description": "remind me to submit the quarterly report before Friday"}'
```

**Response:**

```json
{
  "title": "Submit quarterly report",
  "description": "Ensure the quarterly report is completed and submitted before the deadline",
  "dueDate": "2026-05-01",
  "priority": "HIGH",
  "status": "TODO"
}
```

`priority` is inferred from urgency cues in the description. `dueDate` is resolved relative to today's date when a timeframe is mentioned. If no date is mentioned, `dueDate` will be `null`.

If `OPENAI_API_KEY` is not set, the endpoint returns:

```json
{
  "status": 500,
  "message": "OpenAI API key is not configured. Set the OPENAI_API_KEY environment variable.",
  "timestamp": "..."
}
```

---

## Running the tests

```bash
./mvnw test
```

All tests run without a real OpenAI API key. The service-layer test uses `MockRestServiceServer` to intercept the HTTP call; the integration tests use `@MockBean` to stub the AI service entirely.

---

## H2 console

A browser-based database console is available at `http://localhost:8080/h2-console` while the server is running.

- **JDBC URL:** `jdbc:h2:mem:taskdb`
- **Username:** `sa`
- **Password:** *(leave blank)*
