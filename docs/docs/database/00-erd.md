# SmartStore MVP Database ER Diagram

## Overview

This document describes the Entity Relationship Diagram (ERD) for the **SmartStore MVP** database.

The MVP consists of five core tables:

- Categories
- Products
- Inventory
- Sales
- Sale Items

These tables provide the minimum database structure required to manage products, inventory, and sales operations.

**Note:** This version corrects two inconsistencies found in the original AI-generated ERD, to align it with decisions made during design review:
1. `products.barcode` is now marked **nullable** — not every product (e.g. loose fruits/vegetables) has a manufacturer barcode.
2. `products` now includes the **soft delete** columns (`is_active`, `is_deleted`, `deleted_at`) that were defined in the products table design but missing from the original ERD.

---

# Entity Relationship Diagram

```text
                           +----------------------+
                           |      categories      |
                           +----------------------+
                           | PK id               |
                           | name               |
                           | description        |
                           | created_at         |
                           | updated_at         |
                           +----------+---------+
                                      |
                                      | 1
                                      |
                                      | N
                           +----------v---------+
                           |      products      |
                           +--------------------+
                           | PK id             |
                           | barcode  (nullable) |
                           | name              |
                           | FK category_id    |
                           | purchase_price    |
                           | selling_price     |
                           | unit              |
                           | is_active         |
                           | is_deleted        |
                           | deleted_at        |
                           | created_at        |
                           | updated_at        |
                           +------+------------+
                                  |
                   +--------------+--------------+
                   |                             |
                  1|                            1|
                   |                             |
                   |                             |
        +----------v---------+         +---------v----------+
        |     inventory      |         |     sale_items     |
        +--------------------+         +--------------------+
        | PK id             |         | PK id             |
        | FK product_id     |         | FK sale_id        |
        | quantity          |         | FK product_id     |
        | minimum_quantity  |         | quantity          |
        | created_at        |         | unit_price        |
        | updated_at        |         | line_total        |
        +-------------------+         | created_at        |
                                      | updated_at        |
                                      +---------+---------+
                                                |
                                                | N
                                                |
                                                | 1
                                      +---------v---------+
                                      |       sales       |
                                      +-------------------+
                                      | PK id            |
                                      | invoice_number   |
                                      | sale_date        |
                                      | subtotal         |
                                      | discount         |
                                      | total            |
                                      | payment_method   |
                                      | created_at       |
                                      | updated_at       |
                                      +------------------+
```

---

# Relationships

## Categories → Products
**Relationship Type:** One-to-Many (1:N)
One category can contain many products.
Foreign Key: `products.category_id → categories.id`

## Products → Inventory
**Relationship Type:** One-to-One (1:1)
Each product has exactly one inventory record.
Foreign Key: `inventory.product_id → products.id`

## Products → Sale Items
**Relationship Type:** One-to-Many (1:N)
One product can appear in many sales.
Foreign Key: `sale_items.product_id → products.id`

## Sales → Sale Items
**Relationship Type:** One-to-Many (1:N)
One sale can contain multiple products.
Foreign Key: `sale_items.sale_id → sales.id`

---

# Sales Workflow

```text
Customer buys products
          │
          ▼
Create Sale
          │
          ▼
Create Sale Items
          │
          ▼
Decrease Inventory Quantity
          │
          ▼
Update Reports
```

---

# Database Design Principles

- Separation of Product data and Inventory data.
- Normalized database structure.
- One source of truth for each entity.
- Use of foreign keys to enforce referential integrity.
- Snapshot pricing in sale items to preserve invoice history.
- Soft delete on products to preserve historical sales/invoice integrity.
- Barcode is optional to support unpackaged/weighed items (e.g. produce).
- Support for future expansion without redesigning the database.

---

# Database Relationships Summary

| Parent Table | Child Table | Relationship |
|--------------|-------------|--------------|
| Categories | Products | One-to-Many |
| Products | Inventory | One-to-One |
| Products | Sale Items | One-to-Many |
| Sales | Sale Items | One-to-Many |

---

# MVP Database Tables

1. Categories
2. Products
3. Inventory
4. Sales
5. Sale Items

---

# Notes

- Table names use **snake_case**.
- Column names follow the **snake_case** naming convention.
- English is used for all database objects.
- Monetary values use the **DECIMAL** data type.
- The database is designed to support future modules such as Customers, Suppliers, Purchases, Employees, and Reports.