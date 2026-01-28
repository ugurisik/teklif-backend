# API Documentation

## Base URL
```
/api
```

## Authentication

### POST /auth/login
Kullanıcı girişi yapar.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "user_123",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "TENANT_ADMIN",
      "tenantId": "tenant_123",
      "company": {
        "id": "tenant_123",
        "name": "Acme Corp",
        "logo": "/logos/acme.png"
      }
    },
    "token": "jwt_token_here"
  }
}
```

**Response (401):**
```json
{
  "success": false,
  "error": "Invalid credentials"
}
```

---

### GET /auth/me
Mevcut kullanıcının bilgilerini getirir.

**Headers:** `Authorization: Bearer {token}`

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": "user_123",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "TENANT_ADMIN",
    "tenantId": "tenant_123",
    "company": {
      "id": "tenant_123",
      "name": "Acme Corp",
      "logo": "/logos/acme.png"
    }
  }
}
```

---

### POST /auth/logout
Kullanıcı çıkış yapar.

**Headers:** `Authorization: Bearer {token}`

**Response (200):**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

## Users

### GET /users
Kullanıcı listesini getirir (pagination, filter, search destekler).

**Headers:** `Authorization: Bearer {token}`

**Query Params:**
- `page` (number, default: 1)
- `limit` (number, default: 20)
- `search` (string) - Email, first name, last name ara
- `role` (string) - Role filtrele
- `isActive` (boolean) - Aktif/pasif filtrele
- `tenantId` (string) - Tenant filtrele (sadece SUPER_ADMIN)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "user_123",
        "tenantId": "tenant_123",
        "email": "user@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "role": "TENANT_ADMIN",
        "isActive": true,
        "lastLoginAt": "2025-01-20T10:30:00Z",
        "createdAt": "2025-01-01T09:00:00Z",
        "updatedAt": "2025-01-15T14:20:00Z"
      }
    ],
    "pagination": {
      "total": 45,
      "page": 1,
      "limit": 20,
      "totalPages": 3
    }
  }
}
```

---

### GET /users/:id
Tek bir kullanıcı detayını getirir.

**Headers:** `Authorization: Bearer {token}`

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": "user_123",
    "tenantId": "tenant_123",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "TENANT_ADMIN",
    "isActive": true,
    "lastLoginAt": "2025-01-20T10:30:00Z",
    "createdAt": "2025-01-01T09:00:00Z",
    "updatedAt": "2025-01-15T14:20:00Z"
  }
}
```

---

### POST /users
Yeni kullanıcı oluşturur.

**Headers:** `Authorization: Bearer {token}`

**Request:**
```json
{
  "email": "newuser@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "password": "SecurePass123!",
  "role": "TENANT_USER",
  "tenantId": "tenant_123"
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": "user_456",
    "email": "newuser@example.com",
    "firstName": "Jane",
    "lastName": "Smith",
    "role": "TENANT_USER",
    "isActive": true,
    "createdAt": "2025-01-20T11:00:00Z"
  }
}
```

---

### PUT /users/:id
Kullanıcı bilgilerini günceller.

**Headers:** `Authorization: Bearer {token}`

**Request:**
```json
{
  "firstName": "Jane",
  "lastName": "Johnson",
  "role": "TENANT_ADMIN",
  "isActive": true
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": "user_456",
    "firstName": "Jane",
    "lastName": "Johnson",
    "role": "TENANT_ADMIN",
    "isActive": true,
    "updatedAt": "2025-01-20T11:30:00Z"
  }
}
```

---

### DELETE /users/:id
Kullanıcıyı siler (soft delete).

**Headers:** `Authorization: Bearer {token}`

**Response (200):**
```json
{
  "success": true,
  "message": "User deleted successfully"
}
```

---

### PATCH /users/:id/toggle-status
Kullanıcı durumunu aktif/pasif yapar.

**Headers:** `Authorization: Bearer {token}`

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": "user_456",
    "isActive": false
  }
}
```

---

## Companies (Tenants)

### GET /companies
Tenant (firma) listesini getirir.

**Headers:** `Authorization: Bearer {token}`

**Query Params:**
- `page`, `limit`, `search`, `isActive`

**Response (200):**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "tenant_123",
        "name": "Acme Corp",
        "slug": "acme-corp",
        "logo": "/logos/acme.png",
        "taxNumber": "1234567890",
        "taxOffice": "Vergi Dairesi",
        "email": "info@acme.com",
        "phone": "+90 555 123 4567",
        "address": "İstanbul, Türkiye",
        "package": {
          "name": "Premium",
          "maxUsers": 50,
          "maxOffers": 1000,
          "maxCustomers": 500
        },
        "isActive": true,
        "createdAt": "2025-01-01T09:00:00Z"
      }
    ],
    "pagination": { "total": 10, "page": 1, "limit": 20 }
  }
}
```

---

### GET /companies/:id
Tek bir firma detayını getirir.

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": "tenant_123",
    "name": "Acme Corp",
    "slug": "acme-corp",
    "logo": "/logos/acme.png",
    "taxNumber": "1234567890",
    "taxOffice": "Vergi Dairesi",
    "email": "info@acme.com",
    "phone": "+90 555 123 4567",
    "address": "İstanbul, Türkiye",
    "theme": {
      "primaryColor": "#3b82f6",
      "secondaryColor": "#8b5cf6"
    },
    "emailSettings": {
      "smtpHost": "smtp.gmail.com",
      "smtpPort": 587,
      "smtpUser": "noreply@acme.com",
      "fromEmail": "noreply@acme.com",
      "fromName": "Acme Corp"
    },
    "package": {
      "name": "Premium",
      "maxUsers": 50,
      "maxOffers": 1000,
      "maxCustomers": 500
    },
    "isActive": true,
    "createdAt": "2025-01-01T09:00:00Z",
    "updatedAt": "2025-01-15T14:20:00Z"
  }
}
```

---

### POST /companies
Yeni tenant (firma) oluşturur (sadece SUPER_ADMIN).

**Headers:** `Authorization: Bearer {token}`

**Request:**
```json
{
  "name": "New Corp",
  "slug": "new-corp",
  "taxNumber": "0987654321",
  "taxOffice": "Vergi Dairesi",
  "email": "info@newcorp.com",
  "phone": "+90 555 987 6543",
  "address": "Ankara, Türkiye",
  "package": {
    "name": "Basic",
    "maxUsers": 10,
    "maxOffers": 100,
    "maxCustomers": 50
  }
}
```

---

### PUT /companies/:id
Firma bilgilerini günceller.

---

### DELETE /companies/:id
Firmayı siler (soft delete, sadece SUPER_ADMIN).

---

### POST /companies/:id/logo
Firma logosunu yükler.

**Headers:** `Authorization: Bearer {token}`, `Content-Type: multipart/form-data`

---

## Customers

### GET /customers
Müşteri listesini getirir.

**Headers:** `Authorization: Bearer {token}`

**Query Params:**
- `page`, `limit`, `search`, `type`, `isActive`

**Response (200):**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "cust_123",
        "tenantId": "tenant_123",
        "type": "COMPANY",
        "companyName": "ABC Ltd",
        "contactPerson": "Ahmet Yılmaz",
        "email": "ahmet@abc.com",
        "phone": "+90 555 111 2233",
        "address": "İzmir, Türkiye",
        "taxNumber": "111222333",
        "taxOffice": "Bornova VD",
        "notes": "VIP müşteri",
        "isActive": true,
        "offerCount": 5,
        "createdAt": "2025-01-01T09:00:00Z"
      }
    ],
    "pagination": { "total": 30, "page": 1, "limit": 20 }
  }
}
```

---

### GET /customers/:id
Tek bir müşteri detayını getirir (teklifleri ile birlikte).

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": "cust_123",
    "tenantId": "tenant_123",
    "type": "COMPANY",
    "companyName": "ABC Ltd",
    "contactPerson": "Ahmet Yılmaz",
    "email": "ahmet@abc.com",
    "phone": "+90 555 111 2233",
    "address": "İzmir, Türkiye",
    "taxNumber": "111222333",
    "taxOffice": "Bornova VD",
    "notes": "VIP müşteri",
    "isActive": true,
    "createdAt": "2025-01-01T09:00:00Z",
    "updatedAt": "2025-01-15T14:20:00Z",
    "offers": [
      {
        "id": "offer_123",
        "offerNo": "TKL-20250120-0001",
        "status": "ACCEPTED",
        "total": 15000,
        "createdAt": "2025-01-10T10:00:00Z"
      }
    ]
  }
}
```

---

### POST /customers
Yeni müşteri oluşturur.

**Request (Company):**
```json
{
  "type": "COMPANY",
  "companyName": "XYZ Ltd",
  "contactPerson": "Mehmet Demir",
  "email": "mehmet@xyz.com",
  "phone": "+90 555 444 5566",
  "address": "Ankara, Türkiye",
  "taxNumber": "444555666",
  "taxOffice": "Çankaya VD",
  "notes": ""
}
```

**Request (Individual):**
```json
{
  "type": "INDIVIDUAL",
  "firstName": "Ayşe",
  "lastName": "Kaya",
  "contactPerson": "Ayşe Kaya",
  "email": "ayse@example.com",
  "phone": "+90 555 777 8899"
}
```

---

### PUT /customers/:id
Müşteri bilgilerini günceller.

---

### DELETE /customers/:id
Müşteriyi siler (soft delete).

---

## Products

### GET /products
Ürün/hizmet listesini getirir.

**Query Params:** `page`, `limit`, `search`, `category`, `isActive`

**Response (200):**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "prod_123",
        "tenantId": "tenant_123",
        "code": "URN001",
        "name": "Cappy Şeftalı 330 ML Karton Kutu",
        "description": "Şeftali aromalı meyve suyu",
        "unitPrice": 25.50,
        "currency": "TRY",
        "vatRate": 20,
        "unit": "adet",
        "category": "İçecekler",
        "isActive": true,
        "createdAt": "2025-01-01T09:00:00Z"
      }
    ],
    "pagination": { "total": 100, "page": 1, "limit": 20 }
  }
}
```

---

### POST /products
Yeni ürün/hizmet oluşturur.

---

### PUT /products/:id
Ürün bilgilerini günceller.

---

### DELETE /products/:id
Ürünü siler (soft delete).

---

## Offers

### GET /offers
Teklif listesini getirir.

**Query Params:** `page`, `limit`, `search`, `status`, `customerId`, `startDate`, `endDate`

**Response (200):**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "offer_123",
        "offerNo": "TKL-20250120-0001",
        "tenantId": "tenant_123",
        "customerId": "cust_123",
        "customer": {
          "id": "cust_123",
          "companyName": "ABC Ltd",
          "contactPerson": "Ahmet Yılmaz"
        },
        "status": "SENT",
        "subtotal": 10000,
        "vatTotal": 2000,
        "total": 12000,
        "currency": "TRY",
        "validUntil": "2025-02-20T23:59:59Z",
        "createdAt": "2025-01-20T10:00:00Z"
      }
    ],
    "pagination": { "total": 50, "page": 1, "limit": 20 }
  }
}
```

---

### GET /offers/:id
Tek bir teklif detayını getirir.

---

### POST /offers
Yeni teklif oluşturur.

**Request:**
```json
{
  "customerId": "cust_123",
  "currency": "TRY",
  "validUntil": "2025-02-20T23:59:59Z",
  "notes": "Teklifimiz 15 gün geçerlidir.",
  "linkSettings": {
    "password": null,
    "oneTimeView": false
  },
  "items": [
    {
      "productId": "prod_123",
      "productName": "Cappy Şeftalı 330 ML",
      "description": "Kutu",
      "quantity": 100,
      "unit": "adet",
      "unitPrice": 25.50,
      "vatRate": 20,
      "discountRate": 0
    }
  ]
}
```

---

### PUT /offers/:id
Teklifi günceller.

---

### DELETE /offers/:id
Teklifi siler (soft delete).

---

### POST /offers/:id/send
Teklifi gönderir (status: DRAFT -> SENT).

**Request:**
```json
{
  "to": "customer@example.com"
}
```

---

### POST /offers/:id/duplicate
Teklifi kopyalar.

---

### GET /offers/public/:uuid
Public teklif detayını getirir (auth gerektirmez).

---

### POST /offers/public/:uuid/view
Teklif görüntülendiğini kaydeder (auth gerektirmez).

**Request:**
```json
{
  "name": "Görüntüleyen Kişi"
}
```

---

### POST /offers/public/:uuid/accept
Teklifi kabul eder (auth gerektirmez).

**Request:**
```json
{
  "name": "Kabul Eden Kişi",
  "note": "Kabul ediyoruz, teşekkürler."
}
```

---

### POST /offers/public/:uuid/reject
Teklifi reddeder (auth gerektirmez).

**Request:**
```json
{
  "name": "Reddeden Kişi",
  "note": "Fiyatlar yüksek, düzeltebilir misiniz?"
}
```

---

## Notifications

### GET /notifications
Bildirim listesini getirir.

**Query Params:** `page`, `limit`, `isRead`

**Response (200):**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "notif_123",
        "userId": "user_123",
        "type": "OFFER_ACCEPTED",
        "title": "Teklif Kabul Edildi",
        "message": "TKL-20250120-0001 numaralı teklif kabul edildi.",
        "offerId": "offer_123",
        "isRead": false,
        "createdAt": "2025-01-20T17:00:00Z"
      }
    ],
    "pagination": { "total": 15, "page": 1, "limit": 20 }
  }
}
```

---

### PATCH /notifications/:id/read
Bildirimi okundu olarak işaretler.

---

### PATCH /notifications/read-all
Tüm bildirimleri okundu olarak işaretler.

---

### DELETE /notifications/:id
Bildirimi siler.

---

## Dashboard

### GET /dashboard/stats
Dashboard istatistiklerini getirir.

**Query Params:** `period` (string) - today, week, month, year

**Response (200):**
```json
{
  "success": true,
  "data": {
    "totalOffers": 150,
    "draftOffers": 10,
    "sentOffers": 50,
    "viewedOffers": 30,
    "acceptedOffers": 45,
    "rejectedOffers": 15,
    "expiredOffers": 0,
    "totalRevenue": 450000,
    "pendingRevenue": 150000,
    "acceptedRevenue": 300000,
    "recentOffers": [
      {
        "id": "offer_123",
        "offerNo": "TKL-20250120-0001",
        "customerName": "ABC Ltd",
        "status": "SENT",
        "total": 12000,
        "currency": "TRY",
        "createdAt": "2025-01-20T10:00:00Z"
      }
    ],
    "monthlyStats": [
      { "month": "2024-08", "count": 20, "accepted": 10, "revenue": 50000 },
      { "month": "2024-09", "count": 25, "accepted": 15, "revenue": 75000 }
    ]
  }
}
```

---

## Error Responses

Tüm endpoint'ler için standart error response:

**400 Bad Request:**
```json
{
  "success": false,
  "error": "Validation error"
}
```

**401 Unauthorized:**
```json
{
  "success": false,
  "error": "Unauthorized - Invalid or missing token"
}
```

**403 Forbidden:**
```json
{
  "success": false,
  "error": "Forbidden - Insufficient permissions"
}
```

**404 Not Found:**
```json
{
  "success": false,
  "error": "Resource not found"
}
```

**500 Internal Server Error:**
```json
{
  "success": false,
  "error": "Internal server error",
  "requestId": "req_123"
}
```
