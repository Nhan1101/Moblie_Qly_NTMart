# NT Mart API

Backend REST API cho app Android Studio, đọc trực tiếp từ `NT_MART.db`.

## 1. Cai dat

```powershell
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
```

## 2. Chay server

```powershell
python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Swagger UI:

```text
http://127.0.0.1:8000/docs
```

Neu chay Android Emulator thi base URL thuong la:

```text
http://10.0.2.2:8000
```

Neu dung dien thoai that, thay `127.0.0.1` bang IP LAN cua may tinh, vi du:

```text
http://192.168.1.10:8000
```

## 3. Dua len cloud de khong can bat may

Huong nhanh nhat la deploy len Railway va gan Volume de luu file SQLite.

Canh bao:
- SQLite phu hop demo, do an, hoac app it nguoi dung.
- Neu sau nay nhieu user truy cap dong thoi, nen chuyen sang PostgreSQL.

### Cach deploy len Railway

1. Day source code nay len GitHub.
2. Vao Railway, tao project moi va `Deploy from GitHub repo`.
3. Railway se doc file `railway.json` va chay FastAPI.
4. Tao `Volume` va mount vao duong dan `/data`.
5. Them bien moi truong:

```text
DB_PATH=/data/NT_MART.db
```

Backend da duoc sua san de:
- Neu `/data/NT_MART.db` chua ton tai, no se tu copy tu file goc `NT_MART.db`
- Neu da ton tai, no se dung file tren volume va giu du lieu qua cac lan deploy

6. Trong Railway, vao Networking va `Generate Domain`
7. Ban se nhan duoc URL online, vi du:

```text
https://nt-mart-api-production.up.railway.app
```

Swagger online:

```text
https://nt-mart-api-production.up.railway.app/docs
```

## 4. Endpoint chinh

- `POST /auth/login`
- `GET /users`
- `GET /products`
- `GET /products/{id}`
- `POST /products`
- `PUT /products/{id}`
- `DELETE /products/{id}`
- `GET /products/categories`
- `GET /tickets`
- `GET /tickets/{id}`
- `POST /tickets`
- `GET /stats/overview`

Ton kho duoc dong bo bang trigger SQLite tren bang `sale_ticket_details`, nen khi them dong hoa don thi `products.stock_quantity` se tu dong giam.

## 5. JSON mau

Dang nhap:

```json
{
  "username": "admin",
  "password": "123456"
}
```

Tao hoa don:

```json
{
  "user_id": 1,
  "items": [
    { "product_id": 1, "quantity": 2 },
    { "product_id": 5, "quantity": 1 }
  ]
}
```

## 6. Goi tu Android Retrofit

```kotlin
interface NtMartApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): UserResponse

    @GET("products")
    suspend fun getProducts(): List<ProductResponse>

    @POST("tickets")
    suspend fun createTicket(@Body body: TicketCreateRequest): TicketResponse
}
```

Retrofit base URL:

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://nt-mart-api-production.up.railway.app/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```
