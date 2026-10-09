# Sustain AI Spring Boot Starter

A small Maven project for learning:
- Spring Boot REST routes
- A frontend form served by Spring Boot
- PostgreSQL persistence using `JdbcTemplate` (plain JDBC)
- A Groq AI route that sends the user's name and prompt to Groq
- Environment variables for secrets

This example deliberately uses JDBC, not Hibernate/JPA.

## Requirements

- Java 17 or newer
- Maven 3.6+ (or use your IDE's Maven support)
- PostgreSQL running locally
- A Groq API key from https://console.groq.com/keys

## 1. Create the database

Open pgAdmin Query Tool or `psql` and run:

```sql
CREATE DATABASE sustain_ai;
```

The application creates the `entries` table automatically from `src/main/resources/schema.sql` when it starts.

Default database settings in `application.properties` are:
- database: `sustain_ai`
- username: `postgres`
- password: `postgres`

If your PostgreSQL password differs, set `DB_PASSWORD` as an environment variable before starting the app, or edit `src/main/resources/application-local.properties`. That local file is ignored by Git; do not commit real passwords or API keys.

## 2. Set the Groq API key

Get your key from https://console.groq.com/keys.

Open `src/main/resources/application-local.properties` and replace:

```properties
groq.api-key=put_your_groq_api_key_here
```

with your real key. You can also set the PostgreSQL password in that file:

```properties
spring.datasource.password=your_postgres_password
```

This file is ignored by Git and is loaded automatically using the `local` Spring profile. Never commit it or place the key in frontend code.

The application also supports environment variables as an alternative:

```text
GROQ_API_KEY
GROQ_MODEL
DB_PASSWORD
```

The default model is `openai/gpt-oss-120b`. Set `GROQ_MODEL` only when you need
to use another active Groq model. The previously used
`llama-3.3-70b-versatile` model has been retired for free and developer-tier
usage.

### Windows PowerShell (current terminal only)

```powershell
$env:GROQ_API_KEY="paste_your_real_key_here"
$env:DB_PASSWORD="your_postgres_password"
```

Then run Maven in that same PowerShell window.

### Windows Command Prompt (current window only)

```bat
set GROQ_API_KEY=paste_your_real_key_here
set DB_PASSWORD=your_postgres_password
```

Then run Maven in that same Command Prompt window.

Do not share your key or upload it to GitHub. If it has been exposed, revoke it and create a new one.

## 3. Run the project

Open a terminal in the folder containing `pom.xml`:

```bash
mvn spring-boot:run
```

Or build a JAR and run it:

```bash
mvn clean package
java -jar target/sustain-ai-starter-1.0.0.jar
```

Open this page in your browser:

http://localhost:8080

Do not open `index.html` directly as a file. It should be served by Spring Boot so the `/api/...` routes work.

## 4. Test the features

1. Enter a name and message under "Save data to PostgreSQL".
2. Click "Save to database".
3. The saved entry should appear in the entries list.
4. Enter a name and a prompt under "Ask Groq AI".
5. Click "Ask AI". The name and prompt are sent to the backend, which calls Groq.
6. If the AI endpoint fails, check that `GROQ_API_KEY` is set in the same terminal, the key is valid, the model is available, and your network/API usage is working.

## Files explained

- `pom.xml`: Maven project setup and dependencies.
- `SustainAiApplication.java`: the main class; starts Spring Boot.
- `EntryController.java`: `POST /api/entries` saves form data; `GET /api/entries` fetches saved rows using `JdbcTemplate`.
- `AiController.java`: `POST /api/ai` receives a name and prompt, reads the configured Groq key, calls Groq's chat completions endpoint, and returns the reply.
- `application-local.properties`: local-only API key and database password; ignored by Git.
- `application.properties`: PostgreSQL connection settings.
- `schema.sql`: creates the `entries` table.
- `static/index.html`: the frontend forms and JavaScript `fetch()` calls.

## Routes

- `GET /` — frontend page
- `POST /api/entries` — saves `{ "name": "...", "message": "..." }`
- `GET /api/entries` — returns saved entries
- `POST /api/ai` — accepts `{ "name": "...", "prompt": "..." }` and returns `{ "reply": "..." }`

## Troubleshooting

- `Connection refused`: make sure PostgreSQL is running.
- `password authentication failed`: set `DB_PASSWORD` to your PostgreSQL password.
- `database "sustain_ai" does not exist`: create the database using the SQL above.
- `Groq API key is not configured`: replace `put_your_groq_api_key_here` in `application-local.properties`, or set `GROQ_API_KEY`.
- HTTP 502 from `/api/ai`: check the Groq API response, key, model availability, and account limits.
- `mvn is not recognized`: install Maven or run through an IDE with Maven support.
