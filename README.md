# springAdoption — project rules

Rules for backend (`springAdoption`), database (`pg_queries.sql`), and frontend (`frontend/bank_fe`).

## Database (`pg_queries.sql`)

- **Always append new SQL at the bottom** of `pg_queries.sql`. Do not edit or reorder older blocks for history that may already be applied.
- **Schema changes on existing DBs**: add `ALTER TABLE`, `CREATE TABLE IF NOT EXISTS`, or migration-style statements **below** the marker comment at the end of the file.
- Run new statements manually against your Postgres instance after pulling changes.
- Keep table/column names in `snake_case`; Java records map via Spring Data JDBC.

## Java backend

### Package layout

Feature packages under `com.bank.central` (single Maven module):

| Package | Responsibility |
|---------|----------------|
| `common.exception` | `BusinessException`, `ErrorCode`, global handler |
| `common.constants` | `AppConstants` — statuses and user-facing messages |
| `config` | `@EnableCaching`, CORS, async/virtual-thread executor |
| `auth` | JWT login, credentials, security filters |
| `customer` | Onboarding, customer domain/repos |
| `account` | Accounts, transfers, cash requests |
| `admin` | Customer search, activate/deactivate, cash audit |
| `otp` | OTP generation/verification, Redis store port |
| `notification` | Email/SMS adapters (`MailSender`, `SmsSender` ports) |
| `transaction` | `bank_transaction` records |
| `branch` | Branch/IFSC linkage |

Each feature uses `domain/`, `repository/`, `service/`, `web/`, `dto/` where applicable.

### Layering

- **Controllers** (`web/`) depend on **service interfaces** only.
- **Services** orchestrate repositories and cross-feature interfaces (e.g. `OtpService`, `AuthService`).
- **Ports** (`otp.port.OtpStore`, `notification.port.MailSender` / `SmsSender`) isolate Redis and SMTP/SMS; adapters live in `infra/` or `notification.service/`.
- **`AccountService` split**: `TransferService`, `AccountCreationService`, `CashRequestService`; session OTP gates in `OtpSessionGate`.
- **Async notifications**: `@Async` on mail/SMS adapters; DB work stays synchronous on the request thread.
- **Virtual threads**: `spring.threads.virtual.enabled=true` plus `AsyncConfig` with `Executors.newVirtualThreadPerTaskExecutor()`.

### Code rules

- **Transactions**: use `@Transactional` on service methods that perform multiple DB writes (transfers, account creation, admin cash changes, onboarding steps).
- **Logging**: minimum logs at flow boundaries only — start/success of important actions and business failures (login blocked, transfer, admin cash, approval). Use `INFO` for success paths, `WARN` for expected business blocks. Avoid logging inside tight loops or on every read.
- **Object creation**: prefer **factory/copy methods on records** (`Account.newActive()`, `AdminCustomerAudit.cashAdd()`, `withBalance()`) instead of large `new Record(...)` blocks at call sites.
- **Persistence**: Spring Data JDBC + records with `@Table`. **Redis** for OTP (`StringRedisTemplate`) and `@Cacheable` values via `RedisConfig` — short `@type` ids (e.g. `user-identity`), not Java class names. New cached types: add one `NamedType` line in `RedisConfig.CACHE_VALUE_TYPES`.
- **Admin APIs**: under `/api/v1/admin/**`, `ADMIN` role only. Staff (`EMPLOYEE`) uses cash-request approval only.
- **Admin actions on customers** are stored in `admin_customer_audit` (cash add/remove, activate/deactivate).
- **Constants & messages**: use the single `AppConstants` interface (`common.constants`) for all status strings (`PENDING`, `ACTIVE`, `SUCCESS`, etc.) and user-facing message text. Do not hardcode status or UI messages inline in services/controllers.
- **Imports**: use normal imports at the top of the file. Do not use fully qualified class names in the middle of code (e.g. `com.bank.central.account.domain.CashAccountRequest`).
- **Error messages**: `ErrorCode` holds codes only; message text lives in `AppConstants`. For `BusinessException(ErrorCode)` (no custom message), `GlobalExceptionHandler` resolves text via `AppConstants.errorMessage(errorCode)`.
- **Parameterized messages**: use `%s` / `%d` placeholders in `AppConstants` and replace at runtime with `String.format(AppConstants.SOME_KEY, value)` — do not build messages with string concatenation or helper methods.
- **API responses**: success and error payloads must include a human-readable `message` field from `AppConstants` so the frontend can display backend text directly.

## Auth & sessions

- JWT is primary for API auth; session holds flags like OTP-verified for multi-step flows.
- Deactivated customers (`customer.status = INACTIVE`) cannot log in or call customer APIs.

## Frontend

- API base URL from `VITE_API_BASE_URL` in `frontend/bank_fe/.env`.
- Route guards read `sessionStorage` for auth token (not stale React state after logout).
- Admin customer tools live on `/admin` (ADMIN role only).

## Local run

```bash
# Backend (JDK 25)
export JAVA_HOME="$HOME/.local/jdk-25"
cd springAdoption && mvn spring-boot:run

# Frontend
cd frontend/bank_fe && npm run dev

# Redis
docker start bank-redis   # or local Redis on 6379
```

Apply new SQL from the bottom of `pg_queries.sql` before testing features that depend on new tables.
