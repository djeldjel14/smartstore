# Products Table Design

## Overview

The **Products** table stores all products available in SmartStore.

Each record represents a single product and contains its master information such as barcode, name, category, prices, and unit of measurement.

Stock information is **not stored** in this table. It is managed separately in the **Inventory** table.

Products are never permanently deleted. Instead, SmartStore uses **Soft Delete** to preserve historical data and maintain referential integrity.

Examples:

- Coca-Cola 1L
- Pepsi 2L
- Nutella 750g
- Rice 5kg

---

# Table Information

| Property | Value |
|----------|-------|
| Table Name | products |
| Purpose | Store product master information |
| Primary Key | id |

---

# Columns

| Column | Data Type | Nullable | Description |
|---------|-----------|----------|-------------|
| id | BIGINT | No | Primary Key (Auto Increment) |
| barcode | VARCHAR(50) | No | Product barcode |
| name | VARCHAR(200) | No | Product name |
| category_id | BIGINT | No | Reference to the product category |
| purchase_price | DECIMAL(10,2) | No | Product purchase price |
| selling_price | DECIMAL(10,2) | No | Product selling price |
| unit | VARCHAR(20) | No | Unit of measurement (Piece, Box, Kg, L, etc.) |
| is_active | BOOLEAN | No | Indicates whether the product is available for sale |
| is_deleted | BOOLEAN | No | Soft delete flag |
| deleted_at | TIMESTAMP | Yes | Date and time of deletion |
| created_at | TIMESTAMP | No | Creation date |
| updated_at | TIMESTAMP | No | Last modification date |

---

# Constraints

## Primary Key

- `id`

## Foreign Keys

- `category_id` → `categories.id`

## Unique Constraints

- `barcode`

## Not Null

- `id`
- `barcode`
- `name`
- `category_id`
- `purchase_price`
- `selling_price`
- `unit`
- `is_active`
- `is_deleted`
- `created_at`
- `updated_at`

## Check Constraints

- `purchase_price >= 0`
- `selling_price >= 0`

---

# Default Values

| Column | Default Value |
|---------|---------------|
| is_active | `true` |
| is_deleted | `false` |

---

# Indexes

| Index | Columns |
|--------|---------|
| Primary Key | `id` |
| Unique Index | `barcode` |
| Index | `category_id` |
| Index | `name` |
| Index | `is_deleted` |

---

# Relationships

## Many-to-One (Categories)

Many products belong to one category.

```text
products (N)
      │
      │
      ▼
categories (1)
```

Foreign Key:

```text
category_id → categories.id
```

---

## One-to-One (Inventory)

Each product has exactly one inventory record.

```text
products (1)
      │
      │
      ▼
inventory (1)
```

Foreign Key (Inventory Table):

```text
inventory.product_id → products.id
```

---

# Business Rules

- Every product must belong to one category.
- Every product must have a unique barcode.
- Purchase price cannot be negative.
- Selling price cannot be negative.
- Stock information is managed exclusively through the **Inventory** table.
- Products can be activated or deactivated using the `is_active` field.
- Products are never permanently deleted.
- Soft Delete is implemented using:
    - `is_deleted`
    - `deleted_at`
- Deleted products are excluded from the user interface but remain available for historical records such as invoices and reports.

---

# Future Considerations

The following features may be added in future versions:

- Product image
- Brand
- Supplier reference
- Tax rate
- Expiration date
- Product weight
- Product dimensions
- Multiple selling prices
- Product variants (Size, Color, Flavor)
- QR Code support
- Product description

These features are **not** part of the MVP.

---

# Example Data

| id | barcode | name | category_id | purchase_price | selling_price | unit | is_active | is_deleted |
|----|----------|------|-------------|----------------|---------------|------|-----------|------------|
| 1 | 6131234567890 | Coca-Cola 1L | 1 | 95.00 | 120.00 | Piece | true | false |
| 2 | 6131234567891 | Pepsi 2L | 1 | 150.00 | 180.00 | Piece | true | false |
| 3 | 6131234567892 | Nutella 750g | 2 | 620.00 | 760.00 | Piece | true | false |

---

# Notes

- Table names use **snake_case**.
- Column names follow the **snake_case** naming convention.
- English is used for all database objects.
- Product master data and inventory data are intentionally separated.
- Stock quantities are managed in the **Inventory** table.
- SmartStore uses **Soft Delete** instead of permanent deletion for products.
- This table is part of the SmartStore MVP database schema.