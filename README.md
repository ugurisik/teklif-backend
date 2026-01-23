# Teklif Backend

SaaS tabanlı teklif yönetim sistemi için geliştirilmiş Spring Boot backend uygulaması.

## Overview

Teklif Backend, çok kiracılı (multi-tenant) mimaride çalışan, firmaların müşterilerine ürün/hizmet teklifleri oluşturmasını, göndermesini ve takip etmesini sağlayan modern bir REST API servisidir.

## Tech Stack

| Technology | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 4.0.2 |
| MySQL | 8.x |
| MapStruct | 1.5.5.Final |
| Lombok | 1.18.42 |
| JWT (jjwt) | 0.12.5 |
| SpringDoc OpenAPI | 2.3.0 |

## Features

### Multi-Tenant Architecture
- Her tenant için izole veri yapısı
- Tenant bazlı tema özelleştirmesi
- Tenant bazlı SMTP ayarları
- Paket limitleri (kullanıcı, teklif, müşteri sayısı)

### Authentication & Authorization
- JWT tabanlı kimlik doğrulama
- Rol bazlı yetkilendirme (RBAC):
  - `SUPER_ADMIN` - Sistem yöneticisi
  - `TENANT_ADMIN` - Tenant yöneticisi
  - `TENANT_USER` - Tenant kullanıcısı

### Offer Management
- Teklif oluşturma, düzenleme, silme
- Teklif kopyalama (duplicate)
- Teklif gönderme ve takip
- Public link ile paylaşım
- Teklif durumu takibi (DRAFT, SENT, VIEWED, ACCEPTED, REJECTED, EXPIRED)
- Şifre korumalı teklif linkleri
- Tek seferlik görüntüleme desteği

### Customer Management
- Kurumsal ve bireysel müşteri tipleri
- Müşteri bazlı teklif geçmişi

### Product Management
- Ürün/hizmet katalog yönetimi
- KDV oranlı fiyatlandırma

### Dashboard
- Periyodik istatistikler (günlük, haftalık, aylık, yıllık)

## API Documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`

### Endpoints

#### Authentication
```
POST   /api/auth/login
GET    /api/auth/me
POST   /api/auth/logout
```

#### Offers
```
GET    /api/offers
GET    /api/offers/{id}
POST   /api/offers
POST   /api/offers/{id}/send
POST   /api/offers/{id}/duplicate
DELETE /api/offers/{id}
```

#### Public Offer Links
```
GET    /api/offers/public/{uuid}
POST   /api/offers/public/{uuid}/view
POST   /api/offers/public/{uuid}/accept
POST   /api/offers/public/{uuid}/reject
```

#### Customers
```
GET    /api/customers
GET    /api/customers/{id}
POST   /api/customers
PUT    /api/customers/{id}
DELETE /api/customers/{id}
```

#### Products
```
GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
```

#### Dashboard
```
GET    /api/dashboard/stats?period={period}
```

#### Users
```
GET    /api/users
GET    /api/users/{id}
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
```

## Project Structure

```
src/main/java/com/teklif/app/
├── config/           # Security configuration, data seeder
├── controller/       # REST API controllers
├── dto/
│   ├── request/      # Request DTOs
│   └── response/     # Response DTOs
├── entity/           # JPA entities
├── enums/            # Enumerations
├── exception/        # Custom exceptions
├── mapper/           # MapStruct mappers
├── repository/       # JPA repositories
├── security/         # JWT, filters, user details
├── service/          # Business logic
└── util/             # Utilities (TenantContext)
```

## Installation

### Prerequisites
- Java 21+
- MySQL 8.x
- Maven 3.x

### Database Setup

```sql
CREATE DATABASE teklif_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/teklif_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Secret (change this in production)
jwt.secret=your-secret-key
jwt.expiration=86400000

# Mail Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### Run Application

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using Maven
mvn spring-boot:run

# Build JAR
./mvnw clean package
java -jar target/app-0.0.1-SNAPSHOT.jar
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Server port | 8080 |
| `DB_URL` | Database URL | jdbc:mysql://localhost:3306/teklif_db |
| `DB_USERNAME` | Database username | stk |
| `DB_PASSWORD` | Database password | stk2022 |
| `JWT_SECRET` | JWT signing secret | - |
| `JWT_EXPIRATION` | Token expiration (ms) | 86400000 |

## Entity Model

### Tenant
- Firma bilgileri (ad, vergi no, adres)
- Tema ayarları
- SMTP ayarları
- Paket limitleri

### User
- Tenant kullanıcısı
- Rol bazlı yetkilendirme
- Son giriş takibi

### Customer
- Kurumsal/Bireysel müşteri
- İletişim bilgileri
- Vergi bilgileri

### Product
- Ürün/hizmet katalog
- Fiyat ve KDV oranı

### Offer
- Teklif başlığı ve detayları
- Public UUID ile paylaşım
- Durum takibi
- Aktivite geçmişi (gönderilme, görüntülenme, kabul/red)

### OfferItem
- Teklif kalemleri
- Ürün, miktar, birim fiyat

## Security

- JWT stateless authentication
- Password hashing with BCrypt
- Role-based access control (RBAC)
- Tenant isolation via filter
- Public endpoints for offer viewing

## License

MIT License
