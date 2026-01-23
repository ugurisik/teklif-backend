# API Analysis Report

api.md ile backend kod karşılaştırma analizi.

**Tarih:** 2026-01-23
**Proje:** Teklif Backend SaaS API

---

## ✅ Tam Uyumlu Endpoint'ler

| Modül | Endpoint | Durum |
|-------|----------|-------|
| **Auth** | POST /api/auth/login | ✅ |
| **Auth** | GET /api/auth/me | ✅ |
| **Auth** | POST /api/auth/logout | ✅ |
| **Users** | GET /api/users | ✅ |
| **Users** | GET /api/users/:id | ✅ |
| **Users** | POST /api/users | ✅ |
| **Users** | PUT /api/users/:id | ✅ |
| **Users** | DELETE /api/users/:id | ✅ |
| **Users** | PATCH /api/users/:id/toggle-status | ✅ |
| **Customers** | GET /api/customers | ✅ |
| **Customers** | GET /api/customers/:id | ✅ |
| **Customers** | POST /api/customers | ✅ |
| **Customers** | PUT /api/customers/:id | ✅ |
| **Customers** | DELETE /api/customers/:id | ✅ |
| **Products** | GET /api/products | ✅ |
| **Products** | GET /api/products/:id | ✅ |
| **Products** | POST /api/products | ✅ |
| **Products** | PUT /api/products/:id | ✅ |
| **Products** | DELETE /api/products/:id | ✅ |
| **Offers** | GET /api/offers | ✅ |
| **Offers** | GET /api/offers/:id | ✅ |
| **Offers** | POST /api/offers | ✅ |
| **Offers** | DELETE /api/offers/:id | ✅ |
| **Offers** | POST /api/offers/:id/send | ✅ |
| **Offers** | POST /api/offers/:id/duplicate | ✅ |
| **Public Offers** | GET /api/offers/public/:uuid | ✅ |
| **Public Offers** | POST /api/offers/public/:uuid/view | ✅ |
| **Public Offers** | POST /api/offers/public/:uuid/accept | ✅ |
| **Public Offers** | POST /api/offers/public/:uuid/reject | ✅ |
| **Dashboard** | GET /api/dashboard/stats | ✅ |

---

## ❌ Eksik Endpoint'ler

### Companies (Tamamen Eksik)

```
GET    /api/companies          ← EKSİK
GET    /api/companies/:id      ← EKSİK
POST   /api/companies          ← EKSİK
PUT    /api/companies/:id      ← EKSİK
DELETE /api/companies/:id      ← EKSİK
POST   /api/companies/:id/logo ← EKSİK
```

**Gerekli dosyalar:**
- `CompanyController.java`
- `CompanyService.java`
- DTO'lar: `CompanyRequest.java`, `CompanyResponse.java`
- Not: `TenantRepository` zaten var, kullanılabilir

---

### Notifications (Tamamen Eksik)

```
GET    /api/notifications          ← EKSİK
PATCH  /api/notifications/:id/read ← EKSİK
PATCH  /api/notifications/read-all ← EKSİK
DELETE /api/notifications/:id      ← EKSİK
```

**Gerekli dosyalar:**
- `NotificationController.java`
- `NotificationService.java`
- Not: `NotificationRepository` ve `Notification` entity zaten var

---

### Offers - Update

```
PUT /api/offers/:id  ← EKSİK - Teklif güncelleme
```

**Gerekli değişiklikler:**
- `OfferService.updateOffer()` metodu
- `OfferController.PUT /{id}` endpoint
- `OfferMapper.updateEntity()` metodu

---

## ⚠️ Küçük Farklılıklar

| Endpoint | api.md | Backend | Not |
|----------|--------|---------|-----|
| GET /api/users | `tenantId` query param (SUPER_ADMIN için) | Yok | SUPER_ADMIN tüm tenant'ları filtreleyebilmeli |
| POST /api/offers/:id/send | `{ "to": "email" }` request body | Body yok | Opsiyonel email override eklenebilir |
| GET /api/customers/:id | `offers` array response'da | Kontrol edilmeli | Müşteri detayında teklif listesi isteniyor |

---

## 📋 Öncelik Sırası

1. **Companies Module** - Tenant yönetimi için kritik
2. **Notifications Module** - Bildirim sistemi için
3. **PUT /api/offers/:id** - Teklif güncelleme

---

## 💡 Mevcut Durum Notları

- ✅ `CustomerResponse.offerCount` alanı mevcut
- ✅ `UserResponse.company` alanı mevcut (`CompanyBasicResponse`)
- ✅ `Notification` entity mevcut
- ✅ `Tenant` entity mevcut (Companies için kullanılacak)
- ❌ `CompanyController` yok
- ❌ `NotificationController` yok
- ❌ `OfferController`'da PUT endpoint yok

---

## Kaynak Dosyalar

**Entity'ler:**
- `src/main/java/com/teklif/app/entity/Tenant.java`
- `src/main/java/com/teklif/app/entity/Notification.java`

**Repository'ler:**
- `src/main/java/com/teklif/app/repository/TenantRepository.java`
- `src/main/java/com/teklif/app/repository/NotificationRepository.java`

**Controller'lar:**
- `src/main/java/com/teklif/app/controller/AuthController.java`
- `src/main/java/com/teklif/app/controller/UserController.java`
- `src/main/java/com/teklif/app/controller/CustomerController.java`
- `src/main/java/com/teklif/app/controller/ProductController.java`
- `src/main/java/com/teklif/app/controller/OfferController.java`
- `src/main/java/com/teklif/app/controller/DashboardController.java`
