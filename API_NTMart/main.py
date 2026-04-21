from __future__ import annotations

import os
import shutil
import sqlite3
from contextlib import closing
from datetime import datetime
from pathlib import Path
from typing import Any

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field


BASE_DIR = Path(__file__).resolve().parent
SOURCE_DB_PATH = BASE_DIR / "NT_MART.db"
DB_PATH = Path(os.getenv("DB_PATH", str(SOURCE_DB_PATH))).resolve()


app = FastAPI(
    title="NT Mart API",
    description="REST API cho frontend Android kết nối database NT_MART.db",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def get_connection() -> sqlite3.Connection:
    connection = sqlite3.connect(DB_PATH)
    connection.row_factory = sqlite3.Row
    connection.execute("PRAGMA foreign_keys = ON;")
    return connection


def ensure_database_exists() -> None:
    if DB_PATH.exists():
        return

    DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(SOURCE_DB_PATH, DB_PATH)


def fetch_one(
    connection: sqlite3.Connection, query: str, params: tuple[Any, ...] = ()
) -> dict[str, Any] | None:
    row = connection.execute(query, params).fetchone()
    return dict(row) if row else None


def fetch_all(
    connection: sqlite3.Connection, query: str, params: tuple[Any, ...] = ()
) -> list[dict[str, Any]]:
    rows = connection.execute(query, params).fetchall()
    return [dict(row) for row in rows]


class LoginRequest(BaseModel):
    username: str = Field(min_length=1)
    password: str = Field(min_length=1)


class UserResponse(BaseModel):
    id: int
    username: str
    full_name: str | None = None


class ProductCreate(BaseModel):
    name: str = Field(min_length=1)
    category: str | None = None
    price: int = Field(ge=0)
    stock_quantity: int = Field(ge=0)
    unit: str | None = None
    image_url: str | None = None


class ProductUpdate(BaseModel):
    name: str | None = Field(default=None, min_length=1)
    category: str | None = None
    price: int | None = Field(default=None, ge=0)
    stock_quantity: int | None = Field(default=None, ge=0)
    unit: str | None = None
    image_url: str | None = None


class ProductResponse(BaseModel):
    id: int
    name: str
    category: str | None = None
    price: int
    stock_quantity: int
    unit: str | None = None
    image_url: str | None = None


class TicketItemCreate(BaseModel):
    product_id: int
    quantity: int = Field(gt=0)


class TicketCreate(BaseModel):
    user_id: int
    items: list[TicketItemCreate] = Field(min_length=1)


class TicketItemResponse(BaseModel):
    product_id: int
    product_name: str
    quantity: int
    unit_price: int
    subtotal: int


class TicketResponse(BaseModel):
    id: int
    user_id: int | None = None
    created_at: str
    total_amount: int
    user_name: str | None = None
    items: list[TicketItemResponse] = Field(default_factory=list)


class OverviewResponse(BaseModel):
    total_users: int
    total_products: int
    total_tickets: int
    total_revenue: int


@app.get("/health")
def health_check() -> dict[str, str]:
    return {
        "status": "ok",
        "database": str(DB_PATH),
        "timestamp": datetime.now().isoformat(timespec="seconds"),
    }


@app.on_event("startup")
def on_startup() -> None:
    ensure_database_exists()


@app.post("/auth/login", response_model=UserResponse)
def login(payload: LoginRequest) -> UserResponse:
    with closing(get_connection()) as connection:
        user = fetch_one(
            connection,
            """
            SELECT id, username, full_name
            FROM users
            WHERE username = ? AND password = ?
            """,
            (payload.username, payload.password),
        )

    if not user:
        raise HTTPException(status_code=401, detail="Sai tai khoan hoac mat khau")

    return UserResponse(**user)


@app.get("/users", response_model=list[UserResponse])
def list_users() -> list[UserResponse]:
    with closing(get_connection()) as connection:
        users = fetch_all(
            connection,
            "SELECT id, username, full_name FROM users ORDER BY id",
        )
    return [UserResponse(**user) for user in users]


@app.get("/products", response_model=list[ProductResponse])
def list_products(
    q: str | None = Query(default=None, description="Tim theo ten san pham"),
    category: str | None = Query(default=None),
    limit: int = Query(default=50, ge=1, le=200),
    offset: int = Query(default=0, ge=0),
) -> list[ProductResponse]:
    clauses: list[str] = []
    params: list[Any] = []

    if q:
        clauses.append("name LIKE ?")
        params.append(f"%{q}%")
    if category:
        clauses.append("category = ?")
        params.append(category)

    where_clause = f"WHERE {' AND '.join(clauses)}" if clauses else ""
    params.extend([limit, offset])

    query = f"""
        SELECT id, name, category, price, stock_quantity, unit, image_url
        FROM products
        {where_clause}
        ORDER BY id
        LIMIT ? OFFSET ?
    """

    with closing(get_connection()) as connection:
        products = fetch_all(connection, query, tuple(params))

    return [ProductResponse(**product) for product in products]


@app.get("/products/categories", response_model=list[str])
def list_categories() -> list[str]:
    with closing(get_connection()) as connection:
        rows = connection.execute(
            """
            SELECT DISTINCT category
            FROM products
            WHERE category IS NOT NULL AND TRIM(category) <> ''
            ORDER BY category
            """
        ).fetchall()
    return [row["category"] for row in rows]


@app.get("/products/{product_id}", response_model=ProductResponse)
def get_product(product_id: int) -> ProductResponse:
    with closing(get_connection()) as connection:
        product = fetch_one(
            connection,
            """
            SELECT id, name, category, price, stock_quantity, unit, image_url
            FROM products
            WHERE id = ?
            """,
            (product_id,),
        )

    if not product:
        raise HTTPException(status_code=404, detail="Khong tim thay san pham")

    return ProductResponse(**product)


@app.post("/products", response_model=ProductResponse, status_code=201)
def create_product(payload: ProductCreate) -> ProductResponse:
    with closing(get_connection()) as connection:
        cursor = connection.execute(
            """
            INSERT INTO products(name, category, price, stock_quantity, unit, image_url)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            (
                payload.name,
                payload.category,
                payload.price,
                payload.stock_quantity,
                payload.unit,
                payload.image_url,
            ),
        )
        connection.commit()
        product_id = cursor.lastrowid

        product = fetch_one(
            connection,
            """
            SELECT id, name, category, price, stock_quantity, unit, image_url
            FROM products
            WHERE id = ?
            """,
            (product_id,),
        )

    return ProductResponse(**product)


@app.put("/products/{product_id}", response_model=ProductResponse)
def update_product(product_id: int, payload: ProductUpdate) -> ProductResponse:
    data = payload.model_dump(exclude_none=True)
    if not data:
        raise HTTPException(status_code=400, detail="Khong co du lieu de cap nhat")

    assignments = ", ".join(f"{field} = ?" for field in data)
    params = list(data.values()) + [product_id]

    with closing(get_connection()) as connection:
        existing = fetch_one(connection, "SELECT id FROM products WHERE id = ?", (product_id,))
        if not existing:
            raise HTTPException(status_code=404, detail="Khong tim thay san pham")

        connection.execute(
            f"UPDATE products SET {assignments} WHERE id = ?",
            tuple(params),
        )
        connection.commit()

        product = fetch_one(
            connection,
            """
            SELECT id, name, category, price, stock_quantity, unit, image_url
            FROM products
            WHERE id = ?
            """,
            (product_id,),
        )

    return ProductResponse(**product)


@app.delete("/products/{product_id}")
def delete_product(product_id: int) -> dict[str, str]:
    with closing(get_connection()) as connection:
        used_in_ticket = fetch_one(
            connection,
            "SELECT product_id FROM sale_ticket_details WHERE product_id = ? LIMIT 1",
            (product_id,),
        )
        if used_in_ticket:
            raise HTTPException(
                status_code=409,
                detail="San pham da ton tai trong hoa don, khong the xoa",
            )

        cursor = connection.execute("DELETE FROM products WHERE id = ?", (product_id,))
        connection.commit()

    if cursor.rowcount == 0:
        raise HTTPException(status_code=404, detail="Khong tim thay san pham")

    return {"message": "Xoa san pham thanh cong"}


def build_ticket_response(connection: sqlite3.Connection, ticket_id: int) -> TicketResponse:
    ticket = fetch_one(
        connection,
        """
        SELECT st.id, st.user_id, st.created_at, st.total_amount, u.full_name AS user_name
        FROM sale_tickets st
        LEFT JOIN users u ON u.id = st.user_id
        WHERE st.id = ?
        """,
        (ticket_id,),
    )
    if not ticket:
        raise HTTPException(status_code=404, detail="Khong tim thay hoa don")

    items = fetch_all(
        connection,
        """
        SELECT d.product_id, p.name AS product_name, d.quantity, d.unit_price, d.subtotal
        FROM sale_ticket_details d
        JOIN products p ON p.id = d.product_id
        WHERE d.ticket_id = ?
        ORDER BY d.product_id
        """,
        (ticket_id,),
    )

    ticket["items"] = items
    return TicketResponse(**ticket)


@app.get("/tickets", response_model=list[TicketResponse])
def list_tickets(limit: int = Query(default=20, ge=1, le=100)) -> list[TicketResponse]:
    with closing(get_connection()) as connection:
        ticket_rows = fetch_all(
            connection,
            """
            SELECT st.id
            FROM sale_tickets st
            ORDER BY st.id DESC
            LIMIT ?
            """,
            (limit,),
        )
        return [build_ticket_response(connection, row["id"]) for row in ticket_rows]


@app.get("/tickets/{ticket_id}", response_model=TicketResponse)
def get_ticket(ticket_id: int) -> TicketResponse:
    with closing(get_connection()) as connection:
        return build_ticket_response(connection, ticket_id)


@app.post("/tickets", response_model=TicketResponse, status_code=201)
def create_ticket(payload: TicketCreate) -> TicketResponse:
    with closing(get_connection()) as connection:
        user = fetch_one(connection, "SELECT id FROM users WHERE id = ?", (payload.user_id,))
        if not user:
            raise HTTPException(status_code=404, detail="Khong tim thay nguoi dung")

        merged_items: dict[int, int] = {}
        for item in payload.items:
            merged_items[item.product_id] = merged_items.get(item.product_id, 0) + item.quantity

        product_ids = list(merged_items)
        placeholders = ",".join("?" for _ in product_ids)
        product_rows = fetch_all(
            connection,
            f"""
            SELECT id, name, price, stock_quantity
            FROM products
            WHERE id IN ({placeholders})
            """,
            tuple(product_ids),
        )
        products = {row["id"]: row for row in product_rows}

        if len(products) != len(set(product_ids)):
            raise HTTPException(status_code=404, detail="Mot hoac nhieu san pham khong ton tai")

        line_items: list[dict[str, Any]] = []
        total_amount = 0

        for product_id, quantity in merged_items.items():
            product = products[product_id]
            if product["stock_quantity"] < quantity:
                raise HTTPException(
                    status_code=400,
                    detail=f"San pham '{product['name']}' khong du ton kho",
                )

            subtotal = product["price"] * quantity
            total_amount += subtotal
            line_items.append(
                {
                    "product_id": product_id,
                    "quantity": quantity,
                    "unit_price": product["price"],
                    "subtotal": subtotal,
                }
            )

        try:
            cursor = connection.execute(
                """
                INSERT INTO sale_tickets(user_id, total_amount)
                VALUES (?, ?)
                """,
                (payload.user_id, total_amount),
            )
            ticket_id = cursor.lastrowid

            for item in line_items:
                connection.execute(
                    """
                    INSERT INTO sale_ticket_details(ticket_id, product_id, quantity, unit_price, subtotal)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    (
                        ticket_id,
                        item["product_id"],
                        item["quantity"],
                        item["unit_price"],
                        item["subtotal"],
                    ),
                )

            connection.commit()
        except sqlite3.Error as exc:
            connection.rollback()
            raise HTTPException(status_code=500, detail=f"Loi database: {exc}") from exc

        return build_ticket_response(connection, ticket_id)

@app.put("/tickets/{ticket_id}", response_model=TicketResponse)
def update_ticket(ticket_id: int, payload: TicketCreate) -> TicketResponse:
    with closing(get_connection()) as connection:
        # 1. Kiểm tra phiếu có tồn tại không
        existing_ticket = fetch_one(connection, "SELECT id FROM sale_tickets WHERE id = ?", (ticket_id,))
        if not existing_ticket:
            raise HTTPException(status_code=404, detail="Khong tim thay hoa don de cap nhat")

        # 2. Xóa các chi tiết cũ và HOÀN LẠI tồn kho cũ (Trigger tự động làm nếu có, hoặc làm thủ công ở đây)
        # Để đơn giản và an toàn, ta sẽ xử lý thủ công việc hoàn lại kho cũ trước khi xóa
        old_items = fetch_all(connection, "SELECT product_id, quantity FROM sale_ticket_details WHERE ticket_id = ?", (ticket_id,))
        for item in old_items:
            connection.execute("UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?", (item["quantity"], item["product_id"]))

        connection.execute("DELETE FROM sale_ticket_details WHERE ticket_id = ?", (ticket_id,))

        # 3. Tính toán lại giống hệt POST
        merged_items: dict[int, int] = {}
        for item in payload.items:
            merged_items[item.product_id] = merged_items.get(item.product_id, 0) + item.quantity

        total_amount = 0
        for product_id, quantity in merged_items.items():
            product = fetch_one(connection, "SELECT name, price, stock_quantity FROM products WHERE id = ?", (product_id,))
            if not product:
                raise HTTPException(status_code=404, detail=f"San pham {product_id} khong ton tai")

            if product["stock_quantity"] < quantity:
                raise HTTPException(status_code=400, detail=f"San pham '{product['name']}' khong du ton kho")

            subtotal = product["price"] * quantity
            total_amount += subtotal

            # Lưu chi tiết mới
            connection.execute(
                "INSERT INTO sale_ticket_details(ticket_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)",
                (ticket_id, product_id, quantity, product["price"], subtotal)
            )
            # Trừ kho mới
            connection.execute("UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?", (quantity, product_id))

        # 4. Cập nhật tổng tiền vào phiếu
        connection.execute("UPDATE sale_tickets SET total_amount = ? WHERE id = ?", (total_amount, ticket_id))

        connection.commit()
        return build_ticket_response(connection, ticket_id)

@app.delete("/tickets/{ticket_id}")
def delete_ticket(ticket_id: int) -> dict[str, str]:
    with closing(get_connection()) as connection:
        # 1. Kiểm tra phiếu có tồn tại không
        existing_ticket = fetch_one(connection, "SELECT id FROM sale_tickets WHERE id = ?", (ticket_id,))
        if not existing_ticket:
            raise HTTPException(status_code=404, detail="Khong tim thay hoa don de xoa")

        # 2. Hoàn lại tồn kho cho các sản phẩm trong phiếu
        items = fetch_all(connection, "SELECT product_id, quantity FROM sale_ticket_details WHERE ticket_id = ?", (ticket_id,))
        for item in items:
            connection.execute(
                "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?",
                (item["quantity"], item["product_id"])
            )

        # 3. Xóa chi tiết và phiếu
        connection.execute("DELETE FROM sale_ticket_details WHERE ticket_id = ?", (ticket_id,))
        connection.execute("DELETE FROM sale_tickets WHERE id = ?", (ticket_id,))

        connection.commit()

    return {"message": "Xoa hoa don thanh cong"}

@app.get("/stats/overview", response_model=OverviewResponse)
def overview() -> OverviewResponse:
    with closing(get_connection()) as connection:
        stats = {
            "total_users": connection.execute("SELECT COUNT(*) FROM users").fetchone()[0],
            "total_products": connection.execute("SELECT COUNT(*) FROM products").fetchone()[0],
            "total_tickets": connection.execute("SELECT COUNT(*) FROM sale_tickets").fetchone()[0],
            "total_revenue": connection.execute(
                "SELECT COALESCE(SUM(total_amount), 0) FROM sale_tickets"
            ).fetchone()[0],
        }
    return OverviewResponse(**stats)
