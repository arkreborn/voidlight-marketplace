Project: Voidlight Marketplace (single-tenant, sellable codebase)
Tech: Java 21, Spring Boot 3.3.x, Maven monorepo multimodule, PostgreSQL, Redis (optional), JWT Security, JPA/Hibernate, MapStruct, Lombok, OpenAPI 3
Architecture: DDD monolith (domain → application → adapters), Ports & Adapters.
Migration: No Flyway for MVP (DEV uses ddl-auto=update; PROD uses SQL dump baseline).
Goal: Ship a reusable MVP fast; clean boundaries for future extensions.

1) North Star & MVP Scope
Deliver an e-commerce B2C MVP:
Auth & Roles: email+password, JWT (HS256), roles: ADMIN, CUSTOMER.
Catalog: Category (2-level), Product, SKU/Variant (JSON attributes), Price/SalePrice, Stock per SKU.
Cart & Checkout: cart (anonymous + merge on login), address book, shipping (Flat rate / Pickup), payment v1 (COD & Manual Transfer).
Orders: lifecycle PENDING_PAYMENT → PAID → PROCESSING → SHIPPED → DELIVERED → CANCELLED/REFUNDED; stock reserve on place; release on cancel/expire.
Admin: CRUD Category/Product/SKU/Stock; orders list/detail; voucher (flat/percent) simple.
Notifications: SMTP email for placed/paid/shipped.
Out-of-scope (parkir): payment gateway/ongkir integrations, loyalty/referral, ES search, multi-warehouse.

2) DDD Modules (Monorepo)

voidlight-marketplace/
├─ pom.xml                      # aggregator, BOM import
├─ shared-kernel/               # Money, Result, Errors, Ids, PageSpec
├─ domain/                      # Entities, VOs, Domain Services, Ports
├─ application/                 # UseCases, DTOs, mappers, services
├─ infrastructure/
│  ├─ persistence-jpa/          # Spring Data JPA impl of ports
│  ├─ security/                 # JWT, password encoders, web security
│  ├─ notification-smtp/        # MailPort adapter
│  ├─ payment-cod/              # PaymentPort impl (COD/Manual)
├─ app-api/                     # Spring Boot app (controllers, OpenAPI)
└─ build-support/               # docker-compose (postgres, redis)

Domain Model (core):
User(id, email, passwordHash, roles, createdAt)
CustomerProfile(userId, name, phone)
Address(..., isDefault)
Category(id, parentId, name, slug, position, active)
Product(id, name, slug, description, brand, categoryId, status, images[])
SKU(id, productId, code, attributes(jsonb), price, salePrice, weight, active)
Inventory(skuId, qtyAvailable, qtyReserved)
Cart(customerId?, deviceId?) & CartItem
Voucher(code, type[FLAT/PERCENT], value, minAmount, activeFrom/to, usageLimit, usedCount)
Order(orderNo, status, subtotal, shippingFee, discount, total, paymentMethod, shippingMethod, addressSnapshot, createdAt) & OrderItem
EventLog(type, payload, createdAt)

3) Coding Standards & Conventions
Language: Java 21, records for simple DTO/VO where relevant.
Style: Google Java Style; static analysis with Spotless + Checkstyle (later).
Nullability: Prefer Optional at domain boundaries; validate DTOs via Bean Validation.
Mapping: MapStruct for DTO ↔ entity.
Security: Bcrypt/Argon2 for passwords; JWT HS256; stateless; CORS dev-friendly.
Persistence: JPA entities in infra; domain entities are POJOs; repos are ports.
Transactions: @Transactional at application service layer.
Logging: SLF4J; structured logs (JSON later); no stacktraces in 2xx/4xx logs.
Errors: problem+json style response {type, title, status, detail, errors[]}.
API Docs: springdoc-openapi; Swagger UI in dev profile.

Branch & Commit
main (release), dev (integration), feature branches feat/<scope>.
Conventional Commits: feat:, fix:, chore:, refactor:, test:, docs:.

4) Definition of Done (per feature)
Use case implemented (application service + domain logic + ports).
REST endpoints with request/response DTO + validation + security rules.
JPA implementation & repository tests for critical paths.
Unit tests for domain services (stock reserve, pricing, vouchers).
OpenAPI docs rendered; Swagger route accessible in dev.
schema.sql/data.sql updated if needed; local run succeeds.
Basic email template wired (for related events).

5) Environment & Running

DEV
DB: Postgres (Docker) → build-support/docker-compose.yml.
spring.jpa.hibernate.ddl-auto=update (DEV only).
spring.sql.init.mode=always to load schema.sql/data.sql in app-api.

PROD (MVP)
Generate schema from DEV: pg_dump -s > baseline.sql; apply once.
Set ddl-auto=validate; no Flyway for MVP.

Key ENV Vars
DB_URL, DB_USER, DB_PASS
JWT_SECRET, JWT_EXP_MINUTES
MAIL_HOST, MAIL_USER, MAIL_PASS, MAIL_FROM

6) HTTP Surface (MVP)
Auth
POST /auth/register, POST /auth/login, POST /auth/refresh

Catalog
GET /categories
GET /products (filter: category, q, price_min/max, sort)
GET /products/{slug}
GET /products/{slug}/skus

Cart
GET /cart (token/device)
POST /cart/items
PATCH /cart/items/{id}
DELETE /cart/items/{id}
POST /cart/apply-voucher

Checkout & Orders
POST /checkout
GET /orders
GET /orders/{orderNo}

Admin
POST/PUT/DELETE /admin/categories|products|skus
PATCH /admin/stock/adjust
GET /admin/orders
PATCH /admin/orders/{orderNo}/status
POST /admin/vouchers

7) Task Map (Priority Queue)
Skeleton Monorepo (Maven modules, BOM, shared-kernel scaffolding).
Security (JWT, password encoding, auth endpoints, CORS).
Catalog (domain, ports, JPA impl, controllers + list/search).
Cart (anonymous/cart merge, CRUD, pricing calc).
Checkout/Order (reserve stock, order number, status flow).
Admin CRUD (product/category/sku/stock, order status).
Voucher (validation & application).
SMTP Notifications (placed/paid/shipped).
OpenAPI, Error Handler, Seed data.
Each task must include: acceptance criteria, happy-path tests, minimal error-path tests.

8) Quality Gates (fast but safe)
Unit tests on domain services (≥ basic scenarios).
Controller slice tests for Auth, Catalog, Checkout.
Static analysis compile step (later enable).
Manual smoke script: register → login → browse → add to cart → checkout COD → admin mark PAID → ship → delivered.

9) Packaging & Release
Build fat jar in app-api with dependencies.
Provide docker-compose.yml for DB; Dockerfile for app optional.
Ship baseline.sql, .env.example, and RUNBOOK.md (install & start).
