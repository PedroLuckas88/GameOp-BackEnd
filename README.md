# GameOp Backend

API REST do GameOp (AV2 — Front-end Frameworks).

- Java 21
- Spring Boot 3.4
- Spring Web, Data JPA, Security, Validation
- PostgreSQL + Flyway
- Lombok

Catálogo de jogos vem da **IGDB** (via Twitch), com cache Caffeine. PostgreSQL guarda só usuários, favoritos, reviews e listas.

## Requisitos

- JDK 21
- Maven Wrapper (`mvnw.cmd`)
- PostgreSQL 16+ para **rodar** a API (`database gameop`)

Testes unitários/JPA usam H2 em modo PostgreSQL (não precisam do servidor Postgres).

## Banco

Crie o database antes de `spring-boot:run`:

```sql
CREATE DATABASE gameop;
CREATE USER gameop WITH PASSWORD 'altere-esta-senha';
GRANT ALL PRIVILEGES ON DATABASE gameop TO gameop;
```

O Flyway aplica `V1`–`V4` na subida.

## Variáveis de ambiente

Copie `.env.example`. Não commite `.env`.

| Variável | Default |
|---|---|
| `SERVER_PORT` | `8080` |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/gameop` |
| `DATABASE_USERNAME` | `gameop` |
| `DATABASE_PASSWORD` | *(vazio)* |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` |
| `JWT_SECRET` | chave HS256 (≥ 32 chars) |
| `TWITCH_CLIENT_ID` | Client ID da Twitch (IGDB) |
| `TWITCH_CLIENT_SECRET` | Client Secret — só no ambiente local |

```bat
set DATABASE_PASSWORD=sua-senha
set TWITCH_CLIENT_ID=seu-client-id
set TWITCH_CLIENT_SECRET=seu-client-secret
mvnw.cmd spring-boot:run
```

Catálogo (público): `GET /api/games`, `/api/games/popular`, `/api/games/search?q=Zelda`, `/api/games/{id}`

## Testar persistência

```bat
mvnw.cmd test
```

Health: `GET http://localhost:8080/api/health`
